package net.spell_engine.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.spell_engine.SpellEngineMod;
import net.spell_engine.client.animation.AnimatablePlayer;
import net.spell_engine.internals.casting.SpellCasterEntity;
import net.spell_engine.internals.SpellRegistry;
import net.spell_engine.network.Packets;
import net.spell_engine.particle.ParticleHelper;

public class ClientNetwork {
    public static void initializeHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(Packets.ConfigSync.ID, (client, handler, buf, responseSender) -> {
            var config = Packets.ConfigSync.read(buf);
            SpellEngineMod.config = config;
        });

        ClientPlayNetworking.registerGlobalReceiver(Packets.SpellRegistrySync.ID, (client, handler, buf, responseSender) -> {
            SpellRegistry.decodeContent(buf);
        });

        ClientPlayNetworking.registerGlobalReceiver(Packets.ParticleBatches.ID, (client, handler, buf, responseSender) -> {
            var packet = Packets.ParticleBatches.read(buf);
            var instructions = ParticleHelper.convertToInstructions(client.world, 0, 0, packet);
            client.execute(() -> {
                for(var instruction: instructions) {
                    instruction.perform(client.world);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Packets.SpellAnimation.ID, (client, handler, buf, responseSender) -> {
            var packet = Packets.SpellAnimation.read(buf);
            client.execute(() -> {
                var entity = client.world.getEntityById(packet.playerId());
                if (entity instanceof PlayerEntity player) {
                    ((AnimatablePlayer)player).playSpellAnimation(packet.type(), packet.name(), packet.speed());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Packets.SpellCooldown.ID, (client, handler, buf, responseSender) -> {
            var packet = Packets.SpellCooldown.read(buf);
            client.execute(() -> {
                ((SpellCasterEntity)client.player).getCooldownManager().set(packet.spellId(), packet.duration());
            });
        });
    }
}
