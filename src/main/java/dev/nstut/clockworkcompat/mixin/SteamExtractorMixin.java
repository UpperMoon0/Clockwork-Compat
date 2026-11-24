package dev.nstut.clockworkcompat.mixin;

import dev.nstut.clockworkcompat.SteamConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.clockwork.ClockworkMod;
import org.valkyrienskies.clockwork.content.logistics.gas.generation.steam_generator.SteamGeneratorBlock;
import org.valkyrienskies.clockwork.content.logistics.gas.generation.steam_generator.SteamGeneratorBlockEntity;
import org.valkyrienskies.kelvin.api.DuctNetwork;
import org.valkyrienskies.kelvin.api.DuctNode;
import org.valkyrienskies.kelvin.api.DuctNodePos;
import org.valkyrienskies.kelvin.api.GasType;
import org.valkyrienskies.kelvin.impl.DuctNodeInfo;
import java.util.Map;

@Mixin(value = SteamGeneratorBlockEntity.class, remap = false)
public abstract class SteamExtractorMixin {
    @Inject(method = "tick", at = @At("RETURN"), remap = false)
    private void clockworkcompat$transferGregTechSteam(CallbackInfo ci) {
        SteamGeneratorBlockEntity self = (SteamGeneratorBlockEntity) (Object) this;
        Level level = self.getLevel();
        // Native Create boilers always retain priority.
        if (level == null || level.isClientSide || self.getDataToLoad() != null || self.getTank() != null) return;
        ResourceLocation steamId = new ResourceLocation("gtceu", "steam");
        if (!ForgeRegistries.FLUIDS.containsKey(steamId)) return;
        Fluid steam = ForgeRegistries.FLUIDS.getValue(steamId);
        GasType gas = self.getSteamGas();
        if (steam == null || gas == null) return;

        DuctNetwork<?> network = ClockworkMod.getKelvin(level);
        DuctNodePos pos = self.getDuctNodePosition();
        if (network == null || network.getDisabled() || network.getUnloadedNodes().contains(pos)
                || !network.getNodeInfo().containsKey(pos)) return;
        DuctNode node = network.getNodeAt(pos);
        if (node == null) return;
        double limit = Math.min(node.getMaxPressure(), SteamConversion.MAX_PRESSURE_PA);
        double pressure = network.getPressureAt(pos);
        if (!Double.isFinite(pressure) || pressure < 0 || pressure >= limit) return;
        double temp = steam.getFluidType().getTemperature(new FluidStack(steam, 1));
        if (!SteamConversion.positive(temp) || temp > node.getMaxTemperature()) return;
        double temperature = network.getTemperatureAt(pos);
        double capacity = network.getNodeHeatCapacity(pos);
        if (SteamConversion.positive(capacity)) {
            temperature = Math.max(temperature, network.getHeatEnergy(pos) / capacity);
        }
        double currentCoefficient = 0;
        for (Map.Entry<GasType, Double> entry : network.getGasMassAt(pos).entrySet()) {
            double mass = entry.getValue();
            if (!Double.isFinite(mass) || mass < 0) return;
            GasType type = entry.getKey();
            double coefficient = SteamConversion.pressureCoefficient(type.getSpecificHeatCapacity(), type.getAdiabaticIndex());
            if (!SteamConversion.positive(coefficient)) return;
            currentCoefficient += mass * coefficient;
        }
        DuctNodeInfo info = network.getNodeInfo().get(pos);
        if (info == null) return;
        double volume = node.getVolume() + info.getVolumeChange();
        double coefficientPerMb = SteamConversion.pressureCoefficient(
                gas.getSpecificHeatCapacity(), gas.getAdiabaticIndex()) * SteamConversion.KG_PER_MB;
        int budget = SteamConversion.budget(currentCoefficient, volume, temperature,
                temp, coefficientPerMb, limit);
        if (budget <= 0) return;

        // Keep rejected batches in persisted block-entity state; no re-drain.
        CompoundTag data = self.getPersistentData();
        String pendingKey = "clockworkcompat:pending_steam_mb";
        int pending = data.getInt(pendingKey);
        if (pending < 0 || pending > SteamConversion.MAX_MB_PER_TICK) return;
        if (pending == 0) {
            Direction facing = SteamGeneratorBlock.getFacing(self.getBlockState());
            BlockPos sourcePos = self.getBlockPos().relative(facing.getOpposite());
            if (!level.hasChunkAt(sourcePos)) return;
            BlockEntity source = level.getBlockEntity(sourcePos);
            if (source == null) return;
            IFluidHandler handler = source.getCapability(ForgeCapabilities.FLUID_HANDLER, facing).orElse(null);
            if (handler == null) return;
            FluidStack request = new FluidStack(steam, budget);
            FluidStack simulated = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
            if (simulated.isEmpty() || simulated.getFluid() != steam || simulated.getAmount() > budget) return;
            FluidStack drained = handler.drain(new FluidStack(steam, simulated.getAmount()), IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty() || drained.getFluid() != steam || drained.getAmount() > simulated.getAmount()) return;
            pending = drained.getAmount();
            data.putInt(pendingKey, pending);
            self.setChanged();
        }
        int amount = Math.min(budget, pending);
        if (network.addGasAtTemperature(pos, gas, amount * SteamConversion.KG_PER_MB, temp)) {
            int left = pending - amount;
            if (left == 0) data.remove(pendingKey);
            else data.putInt(pendingKey, left);
            self.setChanged();
        }
    }
}
