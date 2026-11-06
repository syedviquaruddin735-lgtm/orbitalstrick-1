package dev.orbital.orbitalstrike;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fabric port of the non-combat presentation portion of OrbitalStrike.
 * This intentionally provides a visual marker only: no TNT, explosions,
 * entity damage, block damage, or weapon items are created.
 */
public final class OrbitalStrikeMod implements ModInitializer {
    private static final int CHARGE_TICKS = 60;
    private static final int BEAM_HEIGHT = 48;
    private static final double AIM_DISTANCE = 30.0;
    private static final List<Beam> ACTIVE_BEAMS = new ArrayList<>();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("orbital")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("mark")
                                .executes(context -> startMarker(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("help")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Use /orbital mark to create a visual-only beam 30 blocks ahead."),
                                            false);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        ServerTickEvents.END_SERVER_TICK.register(server -> tickBeams());
    }

    private static int startMarker(ServerPlayer player) {
        Vec3 destination = player.position().add(player.getLookAngle().scale(AIM_DISTANCE));
        BlockPos target = BlockPos.containing(destination);
        ServerLevel level = player.serverLevel();

        ACTIVE_BEAMS.add(new Beam(level, target, CHARGE_TICKS));
        level.playSound(null, target, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.65F);
        player.sendSystemMessage(Component.literal("Orbital marker charging (visual effect only)."));
        return Command.SINGLE_SUCCESS;
    }

    private static void tickBeams() {
        Iterator<Beam> iterator = ACTIVE_BEAMS.iterator();
        while (iterator.hasNext()) {
            Beam beam = iterator.next();
            if (beam.level.getServer() == null) {
                iterator.remove();
                continue;
            }

            beam.age++;
            double centerX = beam.target.getX() + 0.5;
            double centerY = beam.target.getY() + 0.5;
            double centerZ = beam.target.getZ() + 0.5;

            // A thin, red-looking column built from vanilla flame particles.
            for (int y = 0; y <= BEAM_HEIGHT; y += 2) {
                beam.level.sendParticles(ParticleTypes.FLAME,
                        centerX, centerY + y, centerZ,
                        2, 0.08, 0.08, 0.08, 0.002);
            }

            // A ring around the marked point makes the target easy to see.
            for (int i = 0; i < 16; i++) {
                double angle = (Math.PI * 2.0 * i) / 16.0;
                double x = centerX + Math.cos(angle) * 2.5;
                double z = centerZ + Math.sin(angle) * 2.5;
                beam.level.sendParticles(ParticleTypes.END_ROD,
                        x, centerY + 0.15, z, 1, 0, 0, 0, 0);
            }

            if (beam.age >= beam.duration) {
                beam.level.sendParticles(ParticleTypes.FIREWORK,
                        centerX, centerY + 1.0, centerZ,
                        30, 1.0, 0.5, 1.0, 0.04);
                beam.level.playSound(null, beam.target, SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.PLAYERS, 1.0F, 1.2F);
                iterator.remove();
            }
        }
    }

    private static final class Beam {
        private final ServerLevel level;
        private final BlockPos target;
        private final int duration;
        private int age;

        private Beam(ServerLevel level, BlockPos target, int duration) {
            this.level = level;
            this.target = target;
            this.duration = duration;
        }
    }
}
