package patriotmc.creative;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CustomChunkGenerator extends ChunkGenerator {
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
                if (chunkX == 0 && chunkZ == 0 || chunkX == -1 && chunkZ == -1 || chunkX == 0 && chunkZ == -1 || chunkX == -1 && chunkZ == 0) {

                    chunkData.setBlock(x, 99, z, Material.STONE);
                    chunkData.setBlock(x, 99, z, Material.STONE);
                    chunkData.setBlock(x, 99, z, Material.STONE);
                    chunkData.setBlock(x, 99, z, Material.STONE);
                    chunkData.setBlock(x, 99, z, Material.STONE);
                    chunkData.setBlock(x, 99, z, Material.STONE);

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
        return new Location(world, 0, 100, 0);
    }
}
