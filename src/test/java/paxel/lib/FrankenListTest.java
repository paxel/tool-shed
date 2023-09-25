package paxel.lib;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class FrankenListTest {

    @Test
    public void testSort() {
        FrankenList<Long> fl = new FrankenList<>();

        final int max = 10_000;
        Random r = new Random(100);
        for (int i = 0; i < max; i++) {
            fl.add((long) r.nextInt(max));
        }

        for (Long value : fl) {
            assertThat(value, is(not(nullValue())));
        }

        fl.sort(null);

        long current = fl.get(0);
        for (int i = 1; i < fl.size(); i++) {
            Long next = fl.get(i);
            if (next < current) {
                fail("Unsorted");
            }
            current = next;
        }
    }

    @Test
    public void testAdd() {
        FrankenList<Long> fl = new FrankenList<>();

        final int max = 10_000;
        for (int i = 0; i < max; i++) {
            fl.add((long) i);
        }

        for (int i = 0; i < fl.size(); i++) {
            Long next = fl.get(i);
            assertThat(next, is((long) i));
        }
    }

    @Test
    public void testInsert() {
        FrankenList<Long> fl = new FrankenList<>();

        final int max = 10_000;
        for (int i = 0; i < max; i++) {
            // all values are inserted add 0, pushing existing data down
            fl.add(0, (long) i);
        }

        for (int i = 0; i < fl.size(); i++) {
            // test the order
            Long next = fl.get(max - 1 - i);
            assertThat(next, is((long) i));
        }
    }

    @Test
    public void testRemove() {
        FrankenList<Long> fl = new FrankenList<>(100);
        ArrayList<Long> comp = new ArrayList<>();

        final int max = 1_000;
        Random r = new Random(100);
        for (int i = 0; i < max; i++) {
            fl.add((long) i);
            comp.add((long) i);
        }

        for (int i = 0; i < max - 1; i++) {
            int nextIndex = r.nextInt(fl.size());
            long removed = fl.remove(nextIndex);
            long removed2 = comp.remove(nextIndex);
            assertThat(removed, is(removed2));
            assertThat(fl, contains(comp.toArray()));
        }

        fl.remove(0);

        assertThat(fl.isEmpty(), is(true));
    }

    @Test
    public void testInsertAfterRemoves() {
        FrankenList<Long> fl = new FrankenList<>(100);
        ArrayList<Integer> rem = new ArrayList<>();

        final int max = 1_000;
        for (int i = 0; i < max; i++) {
            fl.add((long) i);
            rem.add(i);
        }

        Collections.shuffle(rem);

        for (int i = 0; i < rem.size() / 2; i++) {
            fl.remove((int) rem.get(i));
        }

        for (int i = 0; i < rem.size() / 2; i++) {
            final int index = rem.get(i);
            fl.add(i, (long) index);
            assertThat(fl.get(i), is((long) index));
        }
    }

}
