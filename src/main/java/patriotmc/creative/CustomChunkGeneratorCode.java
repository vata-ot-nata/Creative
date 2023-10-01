package patriotmc.creative;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CustomChunkGeneratorCode extends ChunkGenerator {
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return Collections.<BlockPopulator>emptyList();
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunkData = super.createChunkData(world);
        
        // Set biome.
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                biome.setBiome(x, z, Biome.PLAINS);
                if (chunkX >= 0 && chunkX < 16 && chunkZ >= 0 && chunkZ < 16) {
                    if (chunkX == 0) {
                        if (x == 2 && (z + 1) % 3 == 0) {
                            chunkData.setBlock(x, 0, z, 95, (byte) 3);//голубой
                        } else if (x > 3 && x % 2 == 0 && (z + 1) % 3 == 0) {
                            chunkData.setBlock(x, 0, z, 95, (byte) 8);//серый
                        } else {
                            chunkData.setBlock(x, 0, z, 95, (byte) 0);//белый
                        }


                    } else {
                        if (x % 2 == 0 && (z + 1) % 3 == 0) {
                            chunkData.setBlock(x, 0, z, 95, (byte) 8);//серый
                        } else {
                            chunkData.setBlock(x, 0, z, 95, (byte) 0);//белый
                        }
                    }


                }
            }
        }
        // Return the new chunk data.
        return chunkData;
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 1, 0);
    }
}
