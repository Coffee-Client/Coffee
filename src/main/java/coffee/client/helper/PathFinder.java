/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper;

import coffee.client.CoffeeMain;
import lombok.AllArgsConstructor;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PathFinder {
    private static final int[][] offsetsToTry = { { -1, 0, 0 }, { 1, 0, 0 }, { 0, 0, -1 }, { 0, 0, 1 },

        { 0, -1, 0 }, { 0, 1, 0 } };
    public List<BlockPos> visited = new ArrayList<>();
    public Entry startEntry;
    public Entry last;
    BlockPos start, end;
    long startTime;

    public PathFinder(BlockPos start, BlockPos end) {
        this.start = start;
        this.end = end;

        startEntry = addEntry(this.start);
    }

    static boolean isObstructed(Vec3d a, Vec3d b) {
        Vec3d diff = b.subtract(a);
        Box stretch = CoffeeMain.client.player.getBoundingBox().stretch(diff);
        Iterable<VoxelShape> blockCollisions = CoffeeMain.client.world.getBlockCollisions(CoffeeMain.client.player, stretch);
        Iterator<VoxelShape> iterator = blockCollisions.iterator();
        return iterator.hasNext();
        //        Vec3d vec3d = Entity.adjustMovementForCollisions(CoffeeMain.client.player,
        //            diff,
        //            CoffeeMain.client.player.getBoundingBox(),
        //            CoffeeMain.client.world,
        //            new ArrayList<>());
        //        return !vec3d.equals(diff);
        //        assert CoffeeMain.client.player != null;
        //        RaycastContext sc = new RaycastContext(a, b, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, CoffeeMain.client.player);
        //        BlockHitResult raycast = CoffeeMain.client.world.raycast(sc);
        //        return !raycast.getPos().equals(b);
    }

    Entry addEntry(BlockPos p) {
        Entry e = new Entry(p, (float) p.getSquaredDistance(end), null, null);
        addEntry(e);
        return e;
    }

    void addEntry(Entry e) {
        visited.add(e.pos);
        //        while(visited.size() > 1000) visited.remove(0);
        e.previous = last;
        if (last != null) {
            last.next = e;
        }
        last = e;
    }

    public void doForEach(Consumer<Entry> e) {
        Entry p = startEntry;
        while (p != null) {
            e.accept(p);
            p = p.next;
        }
    }

    Entry getFittest() {
        AtomicReference<Entry> fittest = new AtomicReference<>(null);
        doForEach(entry -> {
            if ((fittest.get() == null || entry.calculateFitness() > fittest.get().calculateFitness()) && !getFreeNeighbours(entry).isEmpty()) {
                fittest.set(entry);
            }
        });
        return fittest.get();
    }

    List<Entry> getFreeNeighbours(Entry e) {
        return new ArrayList<>(Arrays.stream(offsetsToTry)
            .map(ints -> e.pos.add(ints[0], ints[1], ints[2]))
            .filter(entry -> !visited.contains(entry))
            .filter(blockPos -> !isObstructed(Vec3d.of(e.pos).add(.5, 0, .5), Vec3d.of(blockPos).add(.5, 0, .5)))
            .map(vec3d -> new Entry(vec3d, (float) vec3d.getSquaredDistance(end), null, e))
            .filter(Entry::canTraverse)
            .toList());
    }

    public boolean find() {
        try {
            this.startTime = System.currentTimeMillis();
            this.visited.clear();
            while (System.currentTimeMillis() - this.startTime < 40_000) {
                Entry fittest = getFittest();
                if (fittest == null) {
                    break;
                }
                fittest.next = null;
                last = fittest;
                Entry nextFittest = getFreeNeighbours(fittest).stream().max(Comparator.comparingDouble(Entry::calculateFitness)).orElseThrow();
                addEntry(nextFittest);
                if (fittest.distance < 1) {
                    return true; // reached goal
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // either timeout or other error
    }

    @AllArgsConstructor
    @Setter
    public static class Entry {
        public BlockPos pos;
        public float distance;
        public Entry next, previous;

        public float calculateFitness() {
            return -(distance);
        }

        public boolean canTraverse() {
            BlockPos above = pos.up();
            BlockState bs = CoffeeMain.client.world.getBlockState(pos);
            BlockState bs1 = CoffeeMain.client.world.getBlockState(above);
            return !bs.getMaterial().blocksMovement() && !bs1.getMaterial().blocksMovement();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Entry entry = (Entry) o;

            return pos.equals(entry.pos);
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }
    }
}
