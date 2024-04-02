package io.github.tt432.startingloot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Mod(StartingLoot.MOD_ID)
@Slf4j
public class StartingLoot {
    public static final String MOD_ID = "starting_loot";

    private final LootTable loot;

    @Data
    @AllArgsConstructor
    public static final class Looted {
        public static final Codec<Looted> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                Codec.BOOL.fieldOf("looted").forGetter(o -> o.looted)
        ).apply(ins, Looted::new));

        boolean looted;
    }

    private final DeferredHolder<AttachmentType<?>, AttachmentType<Looted>> lootedData;

    public StartingLoot(IEventBus bus) {
        DeferredRegister<AttachmentType<?>> register = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
        lootedData = register.register("looted", () ->
                AttachmentType.builder(() -> new Looted(false)).serialize(Looted.CODEC).build());
        register.register(bus);

        try {
            Path path = FMLPaths.CONFIGDIR.get().resolve("starting_loot.json");

            if (!path.toFile().exists()) {
                loot = null;
                log.warn("[Starting Loot] can't found starting_loot.json in config dir.");
                return;
            }

            var lootContent = IOUtils.toString(path.toUri(), StandardCharsets.UTF_8);
            loot = LootTable.CODEC.parse(JsonOps.INSTANCE, GsonHelper.parse(lootContent))
                    .getOrThrow(true, s -> log.error("[Starting Loot] {}", s));
            NeoForge.EVENT_BUS.addListener(this::onJoinLevel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onJoinLevel(PlayerEvent.PlayerLoggedInEvent event) {
        Player p = event.getEntity();
        Level level = p.level();
        Looted data = p.getData(lootedData);

        if (!data.looted && loot != null && !level.isClientSide) {
            data.setLooted(true);

            LootParams lootparams = new LootParams.Builder((ServerLevel) level)
                    .withParameter(LootContextParams.ORIGIN, p.position())
                    .withParameter(LootContextParams.THIS_ENTITY, p)
                    .create(LootContextParamSets.GIFT);
            ObjectArrayList<ItemStack> randomItems = loot.getRandomItems(lootparams);

            for (ItemStack randomItem : randomItems) {
                if (p.addItem(randomItem)) {
                    level.playSound(
                            null,
                            p.getX(),
                            p.getY(),
                            p.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            0.2F,
                            ((p.getRandom().nextFloat() - p.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                    );
                } else {
                    ItemEntity itementity = p.drop(randomItem, false);

                    if (itementity != null) {
                        itementity.setNoPickUpDelay();
                        itementity.setTarget(p.getUUID());
                    }
                }
            }
        }
    }
}
