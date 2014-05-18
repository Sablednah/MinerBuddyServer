package me.sablednah.MinerBuddyServer;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

	/*
    @SuppressWarnings("deprecation")
	public static String JSONLocation(Location l) {
        if (l!=null) {
            String JSON = "";
            JSON = JSON + "{";
            JSON = JSON + "\"X\":" + l.getX() + ",";
            JSON = JSON + "\"Y\":" + l.getY() + ",";
            JSON = JSON + "\"Z\":" + l.getZ() + ",";
            JSON = JSON + "\"Pitch\":" + l.getPitch() + ",";
            JSON = JSON + "\"Yaw\":" + l.getYaw() + ",";
            int bt = l.getWorld().getBlockTypeIdAt(l);
            Material b = Material.getMaterial(bt);
            JSON = JSON + "\"Block\": \"" + b.toString() + "\",";
            int bt2 = l.getWorld().getBlockTypeIdAt(l.subtract(0,1,0));
            Material b2 = Material.getMaterial(bt2);
            JSON = JSON + "\"BlockUnder\": \"" + b2.toString() + "\",";
            JSON = JSON + "\"Biome\": \"" + l.getWorld().getBiome(l.getBlockX(), l.getBlockZ()).toString() + "\",";
            JSON = JSON + "\"BlockLight\":" + l.getBlock().getLightFromBlocks() + ",";
            JSON = JSON + "\"SkyLight\":" + l.getBlock().getLightFromSky() + ",";
            JSON = JSON + "\"Light\":" + l.getBlock().getLightLevel();
            JSON = JSON + "}";
            return JSON;
        }
        return null;
    }
    */

    @SuppressWarnings("deprecation")
 	public static String JSONLocation(ChunkSnapshot c, Location l) {
         if (l!=null) {
             String JSON = "";
             JSON = JSON + "{";
             JSON = JSON + "\"X\":" + l.getX() + ",";
             JSON = JSON + "\"Y\":" + l.getY() + ",";
             JSON = JSON + "\"Z\":" + l.getZ() + ",";
             JSON = JSON + "\"Pitch\":" + l.getPitch() + ",";
             JSON = JSON + "\"Yaw\":" + l.getYaw() + ",";
             int bt = l.getWorld().getBlockTypeIdAt(l);
             Material b = Material.getMaterial(bt);
             JSON = JSON + "\"Block\": \"" + b.toString() + "\",";
             int bt2 = l.getWorld().getBlockTypeIdAt(l.subtract(0,1,0));
             Material b2 = Material.getMaterial(bt2);
             JSON = JSON + "\"BlockUnder\": \"" + b2.toString() + "\",";
             JSON = JSON + "\"Biome\": \"" + l.getWorld().getBiome(l.getBlockX(), l.getBlockZ()).toString() + "\",";

             int baseX = l.getBlockX() % 16;
             int baseZ = l.getBlockZ() % 16;

if (baseX<0) {baseX+=16;}
if (baseZ<0) {baseZ+=16;}

             JSON = JSON + "\"BlockLight\":" + c.getBlockEmittedLight(baseX, l.getBlockY(), baseZ) + ",";
             JSON = JSON + "\"SkyLight\":" + c.getBlockSkyLight(baseX, l.getBlockY(), baseZ) + ",";
             JSON = JSON + "\"Light\":" + Math.max(c.getBlockEmittedLight(baseX, l.getBlockY(), baseZ), c.getBlockSkyLight(baseX, l.getBlockY(), baseZ));
             JSON = JSON + "}";
             return JSON;
         }

         return null;

     }

    public static String JSONPlayerWorldInfo(World w) {
        if (w!=null) {
            String JSON = "";
            JSON = JSON + "{";
            JSON = JSON + "\"Name\":\"" + w.getName() + "\",";
            JSON = JSON + "\"Time\":" + w.getFullTime() + ",";
            JSON = JSON + "\"Weather\":" + w.hasStorm() + ",";
            JSON = JSON + "\"Thunder\":" + w.isThundering() + ",";
            JSON = JSON + "\"WeatherLeft\":" + w.getWeatherDuration() + ",";
            JSON = JSON + "\"ThunderLeft\":" + w.getThunderDuration();
            JSON = JSON + "}";
            return JSON;
        }
        return null;
    }
    
    public static String JSONVelocity(Vector v) {
        if (v!=null) {
            String JSON = "";
            JSON = JSON + "{";
            JSON = JSON + "\"X\":" + v.getX() + ",";
            JSON = JSON + "\"Y\":" + v.getY() + ",";
            JSON = JSON + "\"Z\":" + v.getZ() + ",";
            JSON = JSON + "\"Length\":" + v.length() + ",";
            JSON = JSON + "\"Direction\":" + VectorDirection(v.getX(),v.getZ());
            JSON = JSON + "}";
            return JSON;
        }
        return null;
    }
    
    public static double VectorDirection(double e, double f){
        double d = Math.atan2(e, f) * (180 / Math.PI);
        if(d < 0){ d = 180 - d; }
        return d;
    }
    
    public static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(s.getBytes("UTF-8"));
            BigInteger bigInt = new BigInteger(1, md.digest());
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // This won't happen...
        } catch (UnsupportedEncodingException e) {
            // This also won't happen...
        }
        return "HASHING FAILED";
    }
}
