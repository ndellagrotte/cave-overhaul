package wftech.caveoverhaul;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import wftech.caveoverhaul.mixins.NoiseRouterDataAccessor;
import wftech.caveoverhaul.utils.FabricUtils;

import java.util.Objects;

public class NoiseMaker {

    public static Holder.Reference<DensityFunction> ZERO = null;

    public static DensityFunction makeNoise(DensityFunction functionToCopy){

        return copyDF(functionToCopy, "");
    }

    public static Holder.Reference<DensityFunction> zero() {
        if (ZERO == null) {
            RegistryAccess registries;
            MinecraftServer server = FabricUtils.server;
            registries = server.registryAccess();
            HolderGetter<DensityFunction> hg = registries.lookupOrThrow(Registries.DENSITY_FUNCTION);

            ResourceKey<DensityFunction> zero = NoiseRouterDataAccessor.ZERO();
            ZERO = hg.get(zero).orElseThrow(() -> new IllegalStateException("Zero density function not found"));
        }

        return ZERO;
    }

    public static boolean isHolderOnWhitelist(String holder){

        //String preferred_holder = "minecraft:overworld/noise_router/final_density"; //missing the bottom
        //String preferred_holder = "tectonic:overworld/caves"; // is OK
        //String preferred_holder = "overworld/caves"; <-- overworld_caves covers everything except vanilla cave entrances
        //preferred_holder = "";
        //might need caves/pillars
        //overworld/caves deletes everything except cave entrances
        String[] preferred_holders = {"overworld/caves", "overworld/sloped_cheese"};

        for(String preferred_holder: preferred_holders) {
            if(holder.contains(preferred_holder)) {
                return true;
            }
        }

        return false;

    }

    public static boolean isHolderOnWhitelistEntrances(String holder){
        // List of entries where caves still appear:
        // - 'ResourceKey[minecraft:worldgen/density_function / minecraft:overworld/noise_router/final_density]'
        // - 'ResourceKey[minecraft:worldgen/density_function / tectonic:overworld/caves]'
        return Objects.equals(holder, "") || holder.trim().isEmpty() || holder.contains("overworld/caves");
    }

    public static DensityFunction copyDF(DensityFunction func, String curHolder) {

        //CaveOverhaul.LOGGER.error("-> 1Current holder = " + curHolder);
        //CaveOverhaul.LOGGER.error("***** " + func);

        switch (func) {
            case DensityFunctions.HolderHolder(Holder<DensityFunction> heldFunc) -> {

                boolean hasKey = heldFunc.unwrapKey().isPresent();
                //noodle_ridge_b
                //overworld/caves
                //overworld/spaghetti

                if (hasKey && (heldFunc.unwrapKey().get().identifier().toString().contains("overworld/spaghetti")
                        && isHolderOnWhitelist(curHolder)
                )) {
                    //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                    return new DensityFunctions.HolderHolder(zero());
                }

                if (hasKey && (heldFunc.unwrapKey().get().identifier().toString().contains("cave_entrance")
                        && isHolderOnWhitelistEntrances(curHolder)
                )) {
                    //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                    return new DensityFunctions.HolderHolder(zero());
                }

                //I don't think this does anything at the moment. I need to toy with it to confirm...
                if (hasKey && (heldFunc.unwrapKey().get().identifier().toString().contains("spaghetti_2d")
                        && isHolderOnWhitelist(curHolder)
                )) {
                    //I think this one is acting naughty
                    //int t128 = 64;
                    //.LOGGER.error("-> Current holder = " + curHolder);
                    double d128 = 1.48;
                    //double d128 = 0;
                    return DensityFunctions.constant(d128);
                }
                if (hasKey && (heldFunc.unwrapKey().get().identifier().toString().contains("spaghetti_roughness")
                        && isHolderOnWhitelist(curHolder)
                )) {
                    //int t128 = 1.48;
                    //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                    double d128 = 0.01;
                    //double d128 = 0;
                    return DensityFunctions.constant(d128);
                }

                //I don't think this does anything at the moment. I need to toy with it to confirm...
                if (hasKey && (heldFunc.unwrapKey().get().identifier().toString().contains("noodle")
                        && isHolderOnWhitelist(curHolder)
                )) {
                    //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                    return DensityFunctions.constant(0.4582);
                }

                //Deletes the UGLIEST PART of the new worldgen!!!!!!!!!!
                if (hasKey && (heldFunc.unwrapKey().get().identifier().toString().contains("entrance")
                        && isHolderOnWhitelistEntrances(curHolder)
                )) {
                    //to find a new value if this is throwing errors, set it to a large number and it'll spam
                    //errors with the min/max
                    //CaveOverhaul.LOGGER.error("-> Entrances found with current holder = " + curHolder);
                    return DensityFunctions.constant(1.4999);
                } else {
                    if (hasKey) {
                        heldFunc.unwrapKey().get();
                    }
                    //CaveOverhaul.LOGGER.error("-> Declining to delete entrances found with current holder = '" + curHolder + "'");
                    //CaveOverhaul.LOGGER.error("-> Check 1 = " + (curHolder == ""));
                    //CaveOverhaul.LOGGER.error("-> Check 2 = " + (curHolder.trim() == ""));
                    //CaveOverhaul.LOGGER.error("-> Check 3 = " + (curHolder.trim().isEmpty()));
                    //CaveOverhaul.LOGGER.error("-> Check 4 = " + (isHolderOnWhitelistEntrances(curHolder)));
                }

                if (heldFunc instanceof Holder.Direct<DensityFunction>(DensityFunction newFunc)) {
                    //CaveOverhaul.LOGGER.error("-> Iter on " + newFunc);
                    newFunc = copyDF(newFunc, curHolder);
                    heldFunc = new Holder.Direct<>(newFunc);

                } else if (heldFunc instanceof Holder.Reference<DensityFunction> referenceHolder) {
                    DensityFunction newFunc = referenceHolder.value();
                    copyDF(newFunc, curHolder);
                    DensityFunction value = referenceHolder.value;
                    ResourceKey<DensityFunction> key = referenceHolder.key;
                    String newHolderName = key != null ? key.toString() : "";
                    assert value != null;
                    value = copyDF(value, newHolderName);
                    //retdf = new Holder.Reference<DensityFunction>(type, owner, key, value);
                    heldFunc = new Holder.Direct<>(value);
                }

                return new DensityFunctions.HolderHolder(heldFunc);
            }
            case DensityFunctions.ShiftA t_func -> {

                DensityFunction.NoiseHolder retdf = t_func.offsetNoise();
                return new DensityFunctions.ShiftA(retdf);

            }
            case DensityFunctions.ShiftB t_func -> {
                DensityFunction.NoiseHolder retdf = t_func.offsetNoise();
                return new DensityFunctions.ShiftB(retdf);

            }
            case DensityFunctions.Shift t_func -> {
                DensityFunction.NoiseHolder retdf = t_func.offsetNoise();
                return new DensityFunctions.Shift(retdf);

            }
            case DensityFunctions.Ap2 t_func -> {
                //TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue
                DensityFunction retdf1 = t_func.argument1();
                DensityFunction retdf2 = t_func.argument2();
                retdf1 = copyDF(retdf1, curHolder);
                retdf2 = copyDF(retdf2, curHolder);
                return new DensityFunctions.Ap2(t_func.type(), retdf1, retdf2, t_func.minValue(), t_func.maxValue());
            }
            case DensityFunctions.Marker t_func -> {
                DensityFunction retdf = t_func.wrapped();
                retdf = copyDF(retdf, curHolder);
                return new DensityFunctions.Marker(t_func.type(), retdf);
            }
            case NoiseChunk.Cache2D t_func -> {
                DensityFunction retdf = t_func.wrapped();
                retdf = copyDF(retdf, curHolder);
                return DensityFunctions.cache2d(retdf);
            }
            case NoiseChunk.CacheAllInCell t_func -> {
                DensityFunction retdf = t_func.wrapped();
                retdf = copyDF(retdf, curHolder);
                return DensityFunctions.cacheAllInCell(retdf);
            }
            case NoiseChunk.CacheOnce t_func -> {
                DensityFunction retdf = t_func.wrapped();
                retdf = copyDF(retdf, curHolder);
                return DensityFunctions.cacheOnce(retdf);
            }
            case NoiseChunk.FlatCache t_func -> {
                DensityFunction retdf = t_func.wrapped();
                retdf = copyDF(retdf, curHolder);
                return DensityFunctions.flatCache(retdf);
            }
            case NoiseChunk.NoiseInterpolator t_func -> {
                DensityFunction retdf = t_func.wrapped();
                retdf = copyDF(retdf, curHolder);
                return DensityFunctions.interpolated(retdf);
            }
            case DensityFunctions.Noise t_func -> {
                DensityFunction.NoiseHolder retdf = t_func.noise();
                boolean keyPresent = retdf.noiseData().unwrapKey().isPresent();
                if (keyPresent) {
                    if (retdf.noiseData().unwrapKey().get().identifier().toString().contains("cave_") || //is not causing issues
                            retdf.noiseData().unwrapKey().get().identifier().toString().contains("spaghetti")// || //is not causing issues
                                    //retdf.noiseData().unwrapKey().get().identifier().toString().contains("noodle_")
                                    && isHolderOnWhitelist(curHolder)
                    ) {
                        return zero().value();
                    }
                    if (retdf.noiseData().unwrapKey().get().identifier().toString().contains("noodle_") || //is not causing issues
                            retdf.noiseData().unwrapKey().get().identifier().toString().contains("entrance") //is not causing issues
                                    && isHolderOnWhitelist(curHolder)
                    ) {
                        return DensityFunctions.constant(64);
                    }
                }
                return new DensityFunctions.Noise(retdf, t_func.xzScale(), t_func.yScale());

            }
            case DensityFunctions.EndIslandDensityFunction ignored -> {
                return func;
            }
            case DensityFunctions.WeirdScaledSampler t_func -> {
                //DensityFunction input, DensityFunction.NoiseHolder noise, RarityValueMapper rarityValueMapper
                DensityFunction retdf = t_func.input();
                retdf = copyDF(retdf, curHolder);
                return new DensityFunctions.WeirdScaledSampler(retdf, t_func.noise(), t_func.rarityValueMapper());

            }
            case DensityFunctions.ShiftedNoise t_func -> {
                //DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.NoiseHolder noise
                DensityFunction retdfX = t_func.shiftX();
                DensityFunction retdfY = t_func.shiftY();
                DensityFunction retdfZ = t_func.shiftZ();
                retdfX = copyDF(retdfX, curHolder);
                retdfY = copyDF(retdfY, curHolder);
                retdfZ = copyDF(retdfZ, curHolder);
                return new DensityFunctions.ShiftedNoise(retdfX, retdfY, retdfZ, t_func.xzScale(), t_func.yScale(), t_func.noise());
            }
            case DensityFunctions.RangeChoice t_func -> {
                //DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange
                DensityFunction retInput = t_func.input();
                DensityFunction retIR = t_func.whenInRange();
                DensityFunction retOR = t_func.whenOutOfRange();
                retInput = copyDF(retInput, curHolder);
                retIR = copyDF(retIR, curHolder);
                retOR = copyDF(retOR, curHolder);
                return new DensityFunctions.RangeChoice(retInput, t_func.minInclusive(), t_func.maxExclusive(), retIR, retOR);

            }
            case DensityFunctions.BlendDensity t_func -> {
                DensityFunction retInput = t_func.input();
                retInput = copyDF(retInput, curHolder);
                return new DensityFunctions.BlendDensity(retInput);

            }
            case DensityFunctions.Clamp t_func -> {
                DensityFunction retInput = t_func.input();
                retInput = copyDF(retInput, curHolder);
                return new DensityFunctions.Clamp(retInput, t_func.minValue(), t_func.maxValue());

            }
            case DensityFunctions.Mapped t_func -> {
                DensityFunction retInput = t_func.input();
                retInput = copyDF(retInput, curHolder);
                return new DensityFunctions.Mapped(t_func.type(), retInput, t_func.minValue(), t_func.maxValue());

            }
            case DensityFunctions.Spline t_func -> {
                return new DensityFunctions.Spline(t_func.spline());
            }
            case DensityFunctions.Constant t_func -> {
                return new DensityFunctions.Constant(t_func.value());
            }
            case DensityFunctions.YClampedGradient t_func -> {
                //int fromY, int toY, double fromValue, double toValue
                return new DensityFunctions.YClampedGradient(t_func.fromY(), t_func.toY(), t_func.fromValue(), t_func.toValue());
                //int fromY, int toY, double fromValue, double toValue
            }
            default ->
                    CaveOverhaul.LOGGER.debug("CaveOverhaul: Found other-type density function. Report this to the CaveOverhaul mod maker please -> {}", func.getClass());
        }

        return func;
    }
}