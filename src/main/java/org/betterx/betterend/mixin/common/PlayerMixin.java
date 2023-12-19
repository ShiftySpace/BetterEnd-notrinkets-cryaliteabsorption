package org.betterx.betterend.mixin.common;

import org.betterx.bclib.blocks.BlockProperties;
import org.betterx.bclib.blocks.BlockProperties.TripleShape;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.MHelper;
import org.betterx.betterend.registry.EndBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = Player.class, priority = 200)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static Direction[] be_horizontal;

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "HEAD"), cancellable = true)
    private static void be_findRespawnPositionAndUseSpawnBlock(
            ServerLevel world,
            BlockPos pos,
            float f,
            boolean bl,
            boolean bl2,
            CallbackInfoReturnable<Optional<Vec3>> info
    ) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.is(EndBlocks.RESPAWN_OBELISK)) {
            info.setReturnValue(be_obeliskRespawnPosition(world, pos, blockState));
            info.cancel();
        }
    }

    @Unique
    private static Optional<Vec3> be_obeliskRespawnPosition(ServerLevel world, BlockPos pos, BlockState state) {
        if (state.getValue(BlockProperties.TRIPLE_SHAPE) == TripleShape.TOP) {
            pos = pos.below(2);
        } else if (state.getValue(BlockProperties.TRIPLE_SHAPE) == TripleShape.MIDDLE) {
            pos = pos.below();
        }
        if (be_horizontal == null) {
            be_horizontal = BlocksHelper.makeHorizontal();
        }
        MHelper.shuffle(be_horizontal, world.getRandom());
        for (Direction dir : be_horizontal) {
            BlockPos p = pos.relative(dir);
            BlockState state2 = world.getBlockState(p);
            if (!state2.blocksMotion() && state2.getCollisionShape(world, pos).isEmpty()) {
                return Optional.of(Vec3.atLowerCornerOf(p).add(0.5, 0, 0.5));
            }
        }
        return Optional.empty();
    }
}