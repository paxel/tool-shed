package paxel.bulkexecutor;

import java.util.*;

/**
 * The FrankenList combines an ArrayList and multiple LinkedLists to allow quick navigation to a limited sized
 * linked list and fast add/remove inside that linked list. As a result this list is exceptional faster than either
 * Array or LinkedList for big amounts of data.
 * When elements are inserted to the FrankenList (read: not at the end) the the new element is always added to a LinkedList
 * which is very fast. In case this LinkedList reaches the sectionSizeLimit, it is split in half and the lower half is inserted
 * into the ArrayList containing the sections. This is quite slow, but depending on the size of the sections quite rare.
 * Additionally, all sections behind the LinkedList get their global start index incremented.
 * Searching an element in the FrankenList is searching the correct section in the ArrayList which is fast because random access
 * and then navigating in the small LinkedList to the correct position. This is quite slow.
 * <p>
 * Overall the search and insert times are faster than a pure ArrayList or LinkedList when the size of the map is very big.
 */
public class FrankenList<E> extends AbstractList<E> implements List<E>, RandomAccess {

    private final ArrayListSection<E> data;

    public FrankenList() {
        data = new ArrayListSection<>();
    }

    /**
     * Sets the maximum size of a section. Different sizes can be quicker in different scenarios.
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
    public int size() {
        return data.size();
    }

    @Override
    public E remove(int index) {
        final E remove = data.remove(index);
        if (remove != null) {
            modCount--;
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
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public void add(int index, E value) {
        data.add(index, value);
        modCount++;
    }

    @Override
    public E set(int index, E value) {
        return data.set(index, value);
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

    private class ArrayListSection<E> {

        private final int sectionSizeLimit;

        private final ArrayList<LinkedListSection<E>> sections = new ArrayList<>();

        private int sectionCount;

        private ArrayListSection(int sectionSizeLimit) {
            this.sectionSizeLimit = sectionSizeLimit;
        }

        private ArrayListSection() {
            this.sectionSizeLimit = 750;
        }

        private E remove(int index) {
            if (sections.isEmpty() || index < 0 || index > sectionCount) {
                return null;
            }

            int rootIndex = guessRootIndex(index);
            return dec(sections.get(rootIndex).remove(index), rootIndex);
        }

        private void add(int index, E element) {
            if (index > sectionCount) {
                throw new IndexOutOfBoundsException("Index " + index + " is outside of " + sectionCount);
            }
            if (index == sectionCount) {
                add(element);
            } else {

                int rootIndex = guessRootIndex(index);
                final LinkedListSection<E> section = sections.get(rootIndex);
                section.add(index, element);
                for (int i = rootIndex + 1; i < sections.size(); i++) {
                    sections.get(i).inc();
                }
                if (section.values.size() > sectionSizeLimit) {
                    section.split(sections, rootIndex);
                }
                sectionCount++;
            }
        }

        private E set(int index, E element) {
            if (index >= sectionCount || index < 0) {
                throw new IndexOutOfBoundsException("Index " + index + " is outside of [0 to" + sectionCount + '[');
            }

            int rootIndex = guessRootIndex(index);
            final LinkedListSection<E> bucket = sections.get(rootIndex);
            return bucket.set(index, element);
        }

        private E get(int index) {
            if (sections.isEmpty() || index < 0 || index > sectionCount) {
                throw new IndexOutOfBoundsException("Index " + index + " is outside of [0 to" + sectionCount + '[');
            }
            int rootIndex = guessRootIndex(index);
            return sections.get(rootIndex).get(index);
        }

        private void add(E value) {
            if (sections.isEmpty()) {
                final LinkedListSection<E> bucket = new LinkedListSection<>(0);
                sections.add(bucket);
                bucket.values.add(value);
            } else {
                LinkedListSection<E> last = sections.get(sections.size() - 1);
                if (last.values.size() < sectionSizeLimit) {
                    last.values.add(value);
                } else {
                    LinkedListSection<E> bucket = new LinkedListSection<>(last.globalSectionStartIndex + sectionSizeLimit);
                    bucket.values.add(value);
                    sections.add(bucket);
                }
            }
            sectionCount++;
        }

        private E dec(E removeResult, int currentIndex) {
            if (removeResult != null) {
                // we removed an element, so the indices behind the bucket needs to be decemented
                for (int j = currentIndex + 1; j < sections.size(); j++) {
                    sections.get(j).dec();
                }
                if (sections.get(currentIndex).values.isEmpty()) {
                    // this bucket is now empty. we need to remove it
                    sections.remove(currentIndex);
                }
                sectionCount--;
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
            for (; ; ) {
                LinkedListSection<E> test = sections.get(guessedIndex);
                if (test.globalSectionStartIndex <= index) {
                    if (guessedIndex == lastBucket) {
                        // its in/behind the last one
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
            return sectionCount;
        }

        private class LinkedListSection<E> {

            private int globalSectionStartIndex;
            private final LinkedList<E> values = new LinkedList<>();

            public LinkedListSection(int index) {
                this.globalSectionStartIndex = index;
            }

            private E get(int globalIndex) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex < 0 || localIndex > values.size()) {
                    return null;
                }
                return values.get(localIndex);
            }

            private void add(int globalIndex, E element) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex >= 0 && localIndex <= values.size()) {
                    values.add(localIndex, element);
                }
            }

            private E set(int globalIndex, E element) {
                final int localIndex = globalIndex - this.globalSectionStartIndex;
                if (localIndex < 0 || localIndex > values.size()) {
                    return null;
                } else {
                    return values.set(localIndex, element);
                }
            }

            private E remove(int globalIndex) {
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

            private void split(ArrayList<LinkedListSection<E>> rootList, int splittedNodeIndex) {
                final int nextIndex = sectionSizeLimit / 2;
                LinkedListSection<E> nextNode = new LinkedListSection<>(this.globalSectionStartIndex + nextIndex);
                // move the end of the list to a new bucket
                List<E> subList = values.subList(nextIndex, values.size());
                // add lower part to new bucket
                nextNode.values.addAll(subList);
                // delete lower part in previous bucket
                subList.clear();

                rootList.add(splittedNodeIndex + 1, nextNode);
            }

            @Override
            public String toString() {
                return "Section{" + "indices " + globalSectionStartIndex + " to " + (globalSectionStartIndex + values.size() - 1) + '}';
            }

        }
    }
}