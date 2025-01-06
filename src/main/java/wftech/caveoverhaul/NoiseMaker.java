package wftech.caveoverhaul;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.slf4j.LoggerFactory;
import wftech.caveoverhaul.mixins.NoiseRouterDataAccessor;
import wftech.caveoverhaul.utils.FabricUtils;

import java.lang.reflect.Constructor;
import java.util.Optional;

public class NoiseMaker {

    public static Holder.Reference<DensityFunction> ZERO = null;

    public static DensityFunction makeNoise(DensityFunction functionToCopy){
        DensityFunction newDF = copyDF(functionToCopy, "");

        return newDF;
    }

    public static Holder.Reference<DensityFunction> zero(){

        if(ZERO == null) {

            RegistryAccess registries;
            MinecraftServer server = FabricUtils.server;
            registries = server.registryAccess();
            //HolderGetter.Provider provider = registries.asGetterLookup();
            HolderGetter<DensityFunction> hg = registries.lookup(Registries.DENSITY_FUNCTION).get();

            ResourceKey<DensityFunction> zero = NoiseRouterDataAccessor.ZERO();
            Holder.Reference<DensityFunction> hg_zero = hg.get(zero).get();
            ZERO = hg_zero;
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

        //String preferred_holder = "minecraft:overworld/noise_router/final_density"; //missing the bottom
        //String preferred_holder = "tectonic:overworld/caves"; // is OK
        //String preferred_holder = "overworld/caves"; <-- overworld_caves covers everything except vanilla cave entrances
        //preferred_holder = "";
        //might need caves/pillars
        //overworld/caves deletes everything except cave entrances
        String[] preferred_holders = {""};

        /*
        for(String preferred_holder: preferred_holders) {
            if(holder.contains(preferred_holder)) {
                return true;
            }
        }

         */

        /*
        List of entries where caves still appear:
        Declining to delete entrances found with current holder = 'ResourceKey[minecraft:worldgen/density_function / minecraft:overworld/noise_router/final_density]'
        Declining to delete entrances found with current holder = 'ResourceKey[minecraft:worldgen/density_function / tectonic:overworld/caves]'
         */


        return holder == "" || holder.trim().isEmpty() || holder.contains("overworld/caves");

    }

    public static DensityFunction copyDF(DensityFunction func, String curHolder) {
        //CaveOverhaul.LOGGER.error("-> 1Current holder = " + curHolder);

        //CaveOverhaul.LOGGER.error("***** " + func);

        if (func instanceof DensityFunctions.HolderHolder){
            DensityFunctions.HolderHolder t_func = (DensityFunctions.HolderHolder) func;
            Holder<DensityFunction> heldFunc = t_func.function();

            boolean hasKey = heldFunc.unwrapKey().isPresent();
            //noodle_ridge_b
            //overworld/caves
            //overworld/spaghetti

            if(hasKey && (heldFunc.unwrapKey().get().location().toString().contains("overworld/spaghetti")
                    && isHolderOnWhitelist(curHolder)
            )) {
                //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                return new DensityFunctions.HolderHolder(zero());
            }

            if(hasKey && (heldFunc.unwrapKey().get().location().toString().contains("cave_entrance")
                    && isHolderOnWhitelistEntrances(curHolder)
            )) {
                //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                return new DensityFunctions.HolderHolder(zero());
            }

            //I don't think this does anything at the moment. I need to toy with it to confirm...
            if(hasKey && (heldFunc.unwrapKey().get().location().toString().contains("spaghetti_2d")
                    && isHolderOnWhitelist(curHolder)
            )) {
                //I think this one is acting naughty
                //int t128 = 64;
                //.LOGGER.error("-> Current holder = " + curHolder);
                double d128 = 1.48;
                //double d128 = 0;
                return DensityFunctions.constant(d128);
            }
            if(hasKey && (heldFunc.unwrapKey().get().location().toString().contains("spaghetti_roughness")
                    && isHolderOnWhitelist(curHolder)
            )) {
                //int t128 = 1.48;
                //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                double d128 = 0.01;
                //double d128 = 0;
                return DensityFunctions.constant(d128);
            }

            //I don't think this does anything at the moment. I need to toy with it to confirm...
            if(hasKey && (heldFunc.unwrapKey().get().location().toString().contains("noodle")
                    && isHolderOnWhitelist(curHolder)
            )) {
                //CaveOverhaul.LOGGER.error("-> Current holder = " + curHolder);
                return DensityFunctions.constant(0.4582);
            }

            //Deletes the UGLIEST PART of the new worldgen!!!!!!!!!!
            if(hasKey && (heldFunc.unwrapKey().get().location().toString().contains("entrance")
                    && isHolderOnWhitelistEntrances(curHolder)
            )) {
                //to find a new value if this is throwing errors, set it to a large number and it'll spam
                //errors with the min/max
                //CaveOverhaul.LOGGER.error("-> Entrances found with current holder = " + curHolder);
                return DensityFunctions.constant(1.4999);
            } else if (hasKey && (heldFunc.unwrapKey().get().location().toString().contains("entrance"))) {
                //CaveOverhaul.LOGGER.error("-> Declining to delete entrances found with current holder = '" + curHolder + "'");
                //CaveOverhaul.LOGGER.error("-> Check 1 = " + (curHolder == ""));
                //CaveOverhaul.LOGGER.error("-> Check 2 = " + (curHolder.trim() == ""));
                //CaveOverhaul.LOGGER.error("-> Check 3 = " + (curHolder.trim().isEmpty()));
                //CaveOverhaul.LOGGER.error("-> Check 4 = " + (isHolderOnWhitelistEntrances(curHolder)));
            }

            Holder<DensityFunction> retdf = t_func.function();

            if (retdf instanceof Holder.Direct<DensityFunction>) {
                Holder.Direct<DensityFunction> directHolder = (Holder.Direct<DensityFunction>) retdf;
                DensityFunction newFunc = directHolder.value();
                //CaveOverhaul.LOGGER.error("-> Iter on " + newFunc);
                newFunc = copyDF(newFunc, curHolder);
                retdf = new Holder.Direct<DensityFunction>(newFunc);

            } else if (retdf instanceof Holder.Reference<DensityFunction>) {
                Holder.Reference<DensityFunction> referenceHolder = (Holder.Reference<DensityFunction>) retdf;
                DensityFunction newFunc = referenceHolder.value();
                newFunc = copyDF(newFunc, curHolder);
                Holder.Reference.Type type = referenceHolder.type;
                HolderOwner<DensityFunction> owner = referenceHolder.owner;
                DensityFunction value = referenceHolder.value;
                ResourceKey<DensityFunction> key = referenceHolder.key;
                String newHolderName = key != null ? key.toString() : "";
                value = copyDF(value, newHolderName);
                //retdf = new Holder.Reference<DensityFunction>(type, owner, key, value);
                retdf = new Holder.Direct<DensityFunction>(value);
            }

            return new DensityFunctions.HolderHolder(retdf);

        } else if (func instanceof DensityFunctions.ShiftA){

            DensityFunctions.ShiftA t_func = (DensityFunctions.ShiftA) func;
            DensityFunction.NoiseHolder retdf = t_func.offsetNoise();
            return new DensityFunctions.ShiftA(retdf);

        } else if (func instanceof DensityFunctions.ShiftB){
            DensityFunctions.ShiftB t_func = (DensityFunctions.ShiftB) func;
            DensityFunction.NoiseHolder retdf = t_func.offsetNoise();
            return new DensityFunctions.ShiftB(retdf);

        } else if (func instanceof DensityFunctions.Shift){
            DensityFunctions.Shift t_func = (DensityFunctions.Shift) func;
            DensityFunction.NoiseHolder retdf = t_func.offsetNoise();
            return new DensityFunctions.Shift(retdf);

        } else if (func instanceof DensityFunctions.Ap2){
            //TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue
            DensityFunctions.Ap2 t_func = (DensityFunctions.Ap2) func;
            DensityFunction retdf1 = t_func.argument1();
            DensityFunction retdf2 = t_func.argument2();
            retdf1 = copyDF(retdf1, curHolder);
            retdf2 = copyDF(retdf2, curHolder);
            return new DensityFunctions.Ap2(t_func.type(), retdf1, retdf2, t_func.minValue(), t_func.maxValue());

        } else if (func instanceof DensityFunctions.Marker){
            DensityFunctions.Marker t_func = (DensityFunctions.Marker) func;
            DensityFunction retdf = t_func.wrapped();
            retdf = copyDF(retdf, curHolder);
            return new DensityFunctions.Marker(t_func.type(), retdf);

        } else if (func instanceof NoiseChunk.Cache2D){
            NoiseChunk.Cache2D t_func = (NoiseChunk.Cache2D) func;
            DensityFunction retdf = t_func.wrapped();
            retdf = copyDF(retdf, curHolder);
            try {
                Constructor<?> constructor = NoiseChunk.Cache2D.class.getDeclaredConstructor(DensityFunction.class);
                constructor.setAccessible(true);
                NoiseChunk.Cache2D instance = (NoiseChunk.Cache2D) constructor.newInstance(retdf);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return func;

        } else if (func instanceof NoiseChunk.CacheAllInCell){
            NoiseChunk.CacheAllInCell t_func = (NoiseChunk.CacheAllInCell) func;
            DensityFunction retdf = t_func.wrapped();
            retdf = copyDF(retdf, curHolder);
            try {
                Constructor<?> constructor = NoiseChunk.CacheAllInCell.class.getDeclaredConstructor(DensityFunction.class);
                constructor.setAccessible(true);
                NoiseChunk.CacheAllInCell instance = (NoiseChunk.CacheAllInCell) constructor.newInstance(retdf);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return func;

        } else if (func instanceof NoiseChunk.CacheOnce){
            NoiseChunk.CacheOnce t_func = (NoiseChunk.CacheOnce) func;
            DensityFunction retdf = t_func.wrapped();
            retdf = copyDF(retdf, curHolder);
            try {
                Constructor<?> constructor = NoiseChunk.CacheOnce.class.getDeclaredConstructor(DensityFunction.class);
                constructor.setAccessible(true);
                NoiseChunk.CacheOnce instance = (NoiseChunk.CacheOnce) constructor.newInstance(retdf);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return func;

        } else if (func instanceof NoiseChunk.FlatCache){
            NoiseChunk.FlatCache t_func = (NoiseChunk.FlatCache) func;
            DensityFunction retdf = t_func.wrapped();
            retdf = copyDF(retdf, curHolder);
            try {
                Constructor<?> constructor = NoiseChunk.FlatCache.class.getDeclaredConstructor(DensityFunction.class);
                constructor.setAccessible(true);
                NoiseChunk.FlatCache instance = (NoiseChunk.FlatCache) constructor.newInstance(retdf);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return func;

        } else if (func instanceof NoiseChunk.NoiseInterpolator){
            NoiseChunk.NoiseInterpolator t_func = (NoiseChunk.NoiseInterpolator) func;
            DensityFunction retdf = t_func.wrapped();
            retdf = copyDF(retdf, curHolder);
            try {
                Constructor<?> constructor = NoiseChunk.NoiseInterpolator.class.getDeclaredConstructor(DensityFunction.class);
                constructor.setAccessible(true);
                NoiseChunk.NoiseInterpolator instance = (NoiseChunk.NoiseInterpolator) constructor.newInstance(retdf);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return func;

        } else if (func instanceof DensityFunctions.BlendAlpha){
            return DensityFunctions.BlendAlpha.INSTANCE;

        } else if (func instanceof DensityFunctions.BlendOffset){
            return DensityFunctions.BlendOffset.INSTANCE;

        } else if (func instanceof DensityFunctions.BeardifierMarker){
            return DensityFunctions.BeardifierMarker.INSTANCE;

        }  else if (func instanceof DensityFunctions.Noise){
            DensityFunctions.Noise t_func = (DensityFunctions.Noise) func;
            DensityFunction.NoiseHolder retdf = t_func.noise();
            boolean keyPresent = retdf.noiseData().unwrapKey().isPresent();
            if(keyPresent){
                if (retdf.noiseData().unwrapKey().get().location().toString().contains("cave_") || //is not causing issues
                        retdf.noiseData().unwrapKey().get().location().toString().contains("spaghetti")// || //is not causing issues
                                //retdf.noiseData().unwrapKey().get().location().toString().contains("noodle_")
                                && isHolderOnWhitelist(curHolder)
                ){
                    return zero().value();
                }
                if (retdf.noiseData().unwrapKey().get().location().toString().contains("noodle_") || //is not causing issues
                        retdf.noiseData().unwrapKey().get().location().toString().contains("entrance") //is not causing issues
                                && isHolderOnWhitelist(curHolder)
                ){
                    return DensityFunctions.constant(64);
                }
            }
            return new DensityFunctions.Noise(retdf, t_func.xzScale(), t_func.yScale());

        } else if (func instanceof DensityFunctions.EndIslandDensityFunction){
            DensityFunctions.EndIslandDensityFunction t_func = (DensityFunctions.EndIslandDensityFunction) func;
            return func;

        } else if (func instanceof DensityFunctions.WeirdScaledSampler){
            DensityFunctions.WeirdScaledSampler t_func = (DensityFunctions.WeirdScaledSampler) func;
            //DensityFunction input, DensityFunction.NoiseHolder noise, RarityValueMapper rarityValueMapper
            DensityFunction retdf = t_func.input();
            retdf = copyDF(retdf, curHolder);
            return new DensityFunctions.WeirdScaledSampler(retdf, t_func.noise(), t_func.rarityValueMapper());

        } else if (func instanceof DensityFunctions.ShiftedNoise){
            DensityFunctions.ShiftedNoise t_func = (DensityFunctions.ShiftedNoise) func;
            //DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.NoiseHolder noise
            DensityFunction retdfX = t_func.shiftX();
            DensityFunction retdfY = t_func.shiftY();
            DensityFunction retdfZ = t_func.shiftZ();
            retdfX = copyDF(retdfX, curHolder);
            retdfY = copyDF(retdfY, curHolder);
            retdfZ = copyDF(retdfZ, curHolder);
            return new DensityFunctions.ShiftedNoise(retdfX, retdfY, retdfZ, t_func.xzScale(), t_func.yScale(), t_func.noise());

        } else if (func instanceof DensityFunctions.RangeChoice){
            DensityFunctions.RangeChoice t_func = (DensityFunctions.RangeChoice) func;
            //DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange
            DensityFunction retInput = t_func.input();
            DensityFunction retIR = t_func.whenInRange();
            DensityFunction retOR = t_func.whenOutOfRange();
            retInput = copyDF(retInput, curHolder);
            retIR = copyDF(retIR, curHolder);
            retOR = copyDF(retOR, curHolder);
            return new DensityFunctions.RangeChoice(retInput, t_func.minInclusive(), t_func.maxExclusive(), retIR, retOR);

        } else if (func instanceof DensityFunctions.BlendDensity){
            DensityFunctions.BlendDensity t_func = (DensityFunctions.BlendDensity) func;
            DensityFunction retInput = t_func.input();
            retInput = copyDF(retInput, curHolder);
            return new DensityFunctions.BlendDensity(retInput);

        } else if (func instanceof DensityFunctions.Clamp){
            DensityFunctions.Clamp t_func = (DensityFunctions.Clamp) func;
            DensityFunction retInput = t_func.input();
            retInput = copyDF(retInput, curHolder);
            return new DensityFunctions.Clamp(retInput, t_func.minValue(), t_func.maxValue());

        } else if (func instanceof DensityFunctions.Mapped){
            DensityFunctions.Mapped t_func = (DensityFunctions.Mapped) func;
            DensityFunction retInput = t_func.input();
            retInput = copyDF(retInput, curHolder);
            return new DensityFunctions.Mapped(t_func.type(), retInput, t_func.minValue(), t_func.maxValue());

        } else if (func instanceof DensityFunctions.Spline){
            DensityFunctions.Spline t_func = (DensityFunctions.Spline) func;
            return new DensityFunctions.Spline(t_func.spline());

        } else if (func instanceof DensityFunctions.Constant){
            DensityFunctions.Constant t_func = (DensityFunctions.Constant) func;
            return new DensityFunctions.Constant(t_func.value());

        } else if (func instanceof DensityFunctions.YClampedGradient){
            DensityFunctions.YClampedGradient t_func = (DensityFunctions.YClampedGradient) func;
            //int fromY, int toY, double fromValue, double toValue
            return new DensityFunctions.YClampedGradient(t_func.fromY(), t_func.toY(), t_func.fromValue(), t_func.toValue());

        } else {
            CaveOverhaul.LOGGER.debug("CaveOverhaul: Found other-type density function. Report this to the CaveOverhaul mod maker please -> " + func.getClass());
        }

        return func;
    }
}