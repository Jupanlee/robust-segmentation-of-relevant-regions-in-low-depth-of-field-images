package others.fuzzy.edu.wlu.cs.levy.CG;

import java.util.PriorityQueue;

class NearestNeighborList<T>
{
    PriorityQueue<NeighborEntry<T>> m_Queue;
    int m_Capacity = 0;

    public NearestNeighborList(int capacity)
    {
        this.m_Capacity = capacity;
        this.m_Queue = new PriorityQueue(this.m_Capacity);
    }

    public double getMaxPriority() {
        NeighborEntry p = (NeighborEntry)this.m_Queue.peek();
        return p == null ? (1.0D / 0.0D) : p.value;
    }

    public boolean insert(T object, double priority) {
        if (isCapacityReached()) {
            if (priority > getMaxPriority())
            {
                return false;
            }
            this.m_Queue.add(new NeighborEntry(object, priority));

            this.m_Queue.poll();
        } else {
            this.m_Queue.add(new NeighborEntry(object, priority));
        }
        return true;
    }

    public boolean isCapacityReached() {
        return this.m_Queue.size() >= this.m_Capacity;
    }

    public T getHighest() {
        NeighborEntry p = (NeighborEntry)this.m_Queue.peek();
        return p == null ? null : p.data;
    }

    public boolean isEmpty() {
        return this.m_Queue.size() == 0;
    }

    public int getSize() {
        return this.m_Queue.size();
    }

    public T removeHighest()
    {
        NeighborEntry p = (NeighborEntry)this.m_Queue.poll();
        return p == null ? null : p.data;
    }

    static class NeighborEntry<T>
            implements Comparable<NeighborEntry<T>>
    {
        final T data;
        final double value;

        public NeighborEntry(T data, double value)
        {
            this.data = data;
            this.value = value;
        }

        public int compareTo(NeighborEntry<T> t)
        {
            return Double.compare(t.value, this.value);
        }
    }
}