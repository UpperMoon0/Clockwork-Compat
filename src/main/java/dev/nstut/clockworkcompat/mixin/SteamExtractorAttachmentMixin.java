package dev.nstut.clockworkcompat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Allow the native extractor to survive against a sided Forge fluid source. */
@Mixin(targets = "org.valkyrienskies.clockwork.content.logistics.gas.generation.steam_generator.SteamGeneratorBlock$Companion", remap = false)
public abstract class SteamExtractorAttachmentMixin {
    @Inject(method = "canBlockAttach", at = @At("RETURN"), cancellable = true, remap = false)
    private void clockworkcompat$allowFluidSource(LevelReader reader, BlockPos pos, Direction direction,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        BlockPos source = pos.relative(direction);
        if (!reader.hasChunkAt(source)) return;
        BlockEntity entity = reader.getBlockEntity(source);
        if (entity != null && entity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent())
            cir.setReturnValue(true);
    }
}
