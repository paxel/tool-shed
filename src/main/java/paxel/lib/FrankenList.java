package paxel.lib;

import java.util.*;

/**
 * The FrankenList combines an ArrayList and multiple LinkedLists to allow quick
 * navigation to a limited sized linked list and fast add/remove inside that
 * linked list. As a result, this list is exceptionally faster than either Array or
 * LinkedList for big amounts of data. When elements are inserted to the
 * FrankenList (read: not at the end) the new element is always added to a
 * LinkedList, which is very fast. In case this LinkedList reaches the
 * sectionSizeLimit, it is split in half and the lower half is inserted into the
 * ArrayList containing the sections. This is quite slow, but depending on the
 * size of the sections quite rare. Additionally, all sections behind the
 * LinkedList get their global start index incremented. Searching an element in
 * the FrankenList is searching the correct section in the ArrayList, which is
 * fast because random access and then navigating in the small LinkedList to the
 * correct position. This is quite slow.
 * <p>
 * Overall the search and insert times are faster than a pure ArrayList or
 * LinkedList when the size of the map is very big.
 */
public class FrankenList<E> extends AbstractList<E> implements RandomAccess {

    private final ArrayListSection<E> data;

    public FrankenList() {
        data = new ArrayListSection<>();
    }

    /**
     * Sets the maximum size of a section. Different sizes can be quicker in
     * different scenarios.
     *
     * @param sectionSizeLimit The section size limit.
     */
    public FrankenList(int sectionSizeLimit) {
        data = new ArrayListSection<>(sectionSizeLimit);
    }

    @Override
    public E get(int index) {
        return data.get(index);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public E remove(int index) {
        final E remove = data.remove(index);
        if (remove != null) {
            modCount++;
        }
        return remove;
    }

    @Override
    public boolean add(E value) {
        data.add(value);
        modCount++;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FrankenList<?> other = (FrankenList<?>) obj;
        return Objects.equals(this.data, other.data);
    }

    @Override
    public void add(int index, E value) {
        data.add(index, value);
        modCount++;
    }

    /**
     * Replaces the element at the specified index with the given value.
     *
     * @param index the index of the element to replace
     * @param value the element to be stored at the specified position
     * @return the element previously at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    @Override
    public E set(int index, E value) {
        return data.set(index, value);
    }

    /**
     * Sorts the elements of the list using the specified comparator.
     *
     * @param c the {@code Comparator} used to compare list elements.
     *          A {@code null} value indicates that the elements'
     *          {@linkplain Comparable natural ordering} should be used.
     * @throws ConcurrentModificationException if the list is modified while sorting.
     */
    @Override
    public void sort(Comparator<? super E> c) {
        int expected = super.modCount;
        Object[] a = this.toArray();
        Arrays.sort(a);
        if (super.modCount != expected) {
            throw new ConcurrentModificationException("modified while sorting: sort aborted.");
        }
        clear();
        expected = super.modCount;
        for (Object e : a) {
            data.add((E) e);
        }
        if (super.modCount != expected) {
            throw new ConcurrentModificationException("modified while sorting: list is corrupted.");
        }
        modCount++;
    }

    /**
     * Retrieve the first element.
     *
     * @return The first Element.
     * @throws NoSuchElementException if this list is empty
     */
    public E getFirst() {
        if (data.size() == 0) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    /**
     * Retrieve the last element.
     *
     * @return The last Element.
     * @throws NoSuchElementException if this list is empty
     */
    public E getLast() {
        if (data.size() == 0) {
            throw new NoSuchElementException();
        }
        return data.get(data.size() - 1);
    }

    /**
     * Inserts the element at the beginning.
     *
     * @param value The value
     */
    public void addFirst(E value) {
        modCount++;
        data.add(0, value);
    }

    /**
     * Appends the element at the end.
     *
     * @param value The value
     */
    public void addLast(E value) {
        add(value);

    }

    private class ArrayListSection<F> {

        private final int sectionSizeLimit;

        private final ArrayList<LinkedListSection<F>> sections = new ArrayList<>();

        private int entryCount;

        private ArrayListSection(int sectionSizeLimit) {
            this.sectionSizeLimit = sectionSizeLimit;
        }

        private ArrayListSection() {
            this.sectionSizeLimit = 750;
        }

        private F remove(int index) {
            if (sections.isEmpty() || index < 0 || index > entryCount) {
                return null;
            }

            int rootIndex = guessRootIndex(index);
            return dec(sections.get(rootIndex).remove(index), rootIndex);
        }

        private void add(int index, F element) {
            if (index > entryCount) {
                throw new IndexOutOfBoundsException("Index " + index + " is outside of " + entryCount);
            }
            if (index == entryCount) {
                add(element);
            } else {

                int rootIndex = guessRootIndex(index);
                final LinkedListSection<F> section = sections.get(rootIndex);
                section.add(index, element);
                for (int i = rootIndex + 1; i < sections.size(); i++) {
                    sections.get(i).inc();
                }
                if (section.values.size() > sectionSizeLimit) {
                    section.split(sections, rootIndex);
                }
                entryCount++;
            }
        }

        private F set(int index, F element) {
            if (index >= entryCount || index < 0) {
                throw new IndexOutOfBoundsException("Index " + index + " is outside of [0 to" + entryCount + '[');
            }

            int rootIndex = guessRootIndex(index);
            final LinkedListSection<F> bucket = sections.get(rootIndex);
            return bucket.set(index, element);
        }

        private F get(int index) {
            if (sections.isEmpty() || index < 0 || index > entryCount) {
                throw new IndexOutOfBoundsException("Index " + index + " is outside of [0 to" + entryCount + '[');
            }
            int rootIndex = guessRootIndex(index);
            return sections.get(rootIndex).get(index);
        }

        private void add(F value) {
            if (sections.isEmpty()) {
                final LinkedListSection<F> section = new LinkedListSection<>(0);
                sections.add(section);
                section.values.add(value);
            } else {
                LinkedListSection<F> last = sections.getLast();
                if (last.values.size() < sectionSizeLimit) {
                    last.values.add(value);
                } else {
                    LinkedListSection<F> bucket = new LinkedListSection<>(
                            last.globalSectionStartIndex + sectionSizeLimit);
                    bucket.values.add(value);
                    sections.add(bucket);
                }
            }
            entryCount++;
        }

        private F dec(F removeResult, int currentIndex) {
            if (removeResult != null) {
                // we removed an element, so the indices behind the bucket need to be
                // decremented
                for (int j = currentIndex + 1; j < sections.size(); j++) {
                    sections.get(j).dec();
                }
                if (sections.get(currentIndex).values.isEmpty()) {
                    // this bucket is now empty. we need to remove it
                    sections.remove(currentIndex);
                }
                entryCount--;
            }
            return removeResult;
        }

        private int guessRootIndex(int index) {
            if (index == 0) {
                return 0;
            }
            final int lastBucket = sections.size() - 1;
            if (index > sections.get(lastBucket).globalSectionStartIndex) {
                return lastBucket;
            }

            int guessedIndex = index / sectionSizeLimit;

            if (guessedIndex > lastBucket) {
                guessedIndex = lastBucket;
            }
            if (guessedIndex < 0) {
                guessedIndex = 0;
            }
            for (;;) {
                LinkedListSection<F> test = sections.get(guessedIndex);
                if (test.globalSectionStartIndex <= index) {
                    if (guessedIndex == lastBucket) {
                        // it's in/behind the last one
                        return guessedIndex;
                    }
                    if (sections.get(guessedIndex + 1).globalSectionStartIndex > index) {
                        // found
                        return guessedIndex;
                    }
                    // it is maybe in the next one
                    guessedIndex++;

                } else {
                    if (guessedIndex == 0) {
                        // somethings broken
                        return 0;
                    }
                    // it is maybe in the previous one
                    guessedIndex--;
                }
            }
        }

        int size() {
            return entryCount;
        }

        private void clear() {
            int expected = modCount;
            sections.clear();
            entryCount = 0;
            if (modCount != expected) {
                throw new ConcurrentModificationException("The map was modified while clearing");
            }
            modCount++;
        }

        private class LinkedListSection<G> {

            private int globalSectionStartIndex;
            private final LinkedList<G> values = new LinkedList<>();

            public LinkedListSection(int index) {
                this.globalSectionStartIndex = index;
            }

            private G get(int globalIndex) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex < 0 || localIndex > values.size()) {
                    return null;
                }
                return values.get(localIndex);
            }

            private void add(int globalIndex, G element) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex >= 0 && localIndex <= values.size()) {
                    values.add(localIndex, element);
                }
            }

            private G set(int globalIndex, G element) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex < 0 || localIndex > values.size()) {
                    return null;
                } else {
                    return values.set(localIndex, element);
                }
            }

            private G remove(int globalIndex) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex < 0 || localIndex >= values.size()) {
                    return null;
                }
                return values.remove(localIndex);
            }

            private void inc() {
                globalSectionStartIndex++;
            }

            private void dec() {
                globalSectionStartIndex--;
            }

            private void split(ArrayList<LinkedListSection<G>> rootList, int splitNodeIndex) {
                final int nextIndex = sectionSizeLimit / 2;
                LinkedListSection<G> nextNode = new LinkedListSection<>(this.globalSectionStartIndex + nextIndex);
                // move the end of the list to a new bucket
                List<G> subList = values.subList(nextIndex, values.size());
                // add lower part to the new bucket
                nextNode.values.addAll(subList);
                // delete lower part in previous bucket
                subList.clear();

                rootList.add(splitNodeIndex + 1, nextNode);
            }

            @Override
            public String toString() {
                return "Section{" + "indices " + globalSectionStartIndex + " to "
                        + (globalSectionStartIndex + values.size() - 1) + '}';
            }

        }
    }
}
