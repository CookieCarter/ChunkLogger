package cookie.chunklogger.client;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;

public class ChunkLoggerClient implements ClientModInitializer {
    public static final String MOD_ID = "chunklogger";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // Register the client-side command callback
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("logbedrock")
                .executes(context -> {
                    // Get the source (player) of the command.
                    FabricClientCommandSource source = context.getSource();
                    ClientLevel level = source.getLevel();

                    if (level.dimension() != Level.NETHER) {
                        source.sendError(Component.literal("Command can only be used in the nether."));
                        return 0;
                    }

                    Vec3 playerPos = source.getPosition();

                    final int dist = 16;

                    try (FileOutputStream output = new FileOutputStream("bedrock.txt")) {

                        // Check y-level 4 and 123.
                        for (int y = 4; y <= 123; y += 119) {
                            // Check up to 16 blocks in the x and z axes.
                            for (int x = (int) playerPos.x; x < (int) playerPos.x + dist; x++) {
                                for (int z = (int) playerPos.z; z < (int) playerPos.z + dist; z++) {
                                    BlockPos pos = new BlockPos(x, y, z);
                                    
                                    // Get the block type.
                                    BlockState state = level.getBlockState(pos);
                                    String blockId = state.getBlock().toString();

                                    // Write block to file if it is bedrock.
                                    if (blockId.equals("Block{minecraft:bedrock}")) {
                                        String text = "%d %d %d Bedrock\n".formatted(x, y, z);
                                        output.write(text.getBytes());
                                        // LOGGER.info("Bedrock at [" + x + ", " + y + ", " + z + "]");
                                    }
                                }
                            }
                        }
                        
                        output.close();
                    } catch (IOException e) {
                        LOGGER.error("Failed to write to file!");
                        source.sendError(Component.literal("Failed to write to file!"));
                        return 0;
                    }

                    source.sendFeedback(Component.literal("Blocks logged to bedrock.txt."));
                    
                    return 1; // Command succeeded
                })
            );
        });
    }
}