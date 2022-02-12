package com.unicom.betterworry.util;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class HeapInteger<T> implements Heap<Integer,T>{
    List<Entry<Integer,T>> Heaparr=new CopyOnWriteArrayList();
    AtomicInteger size=new AtomicInteger(0);
    AtomicInteger maxsize=new AtomicInteger(10);
    boolean big=true;
    public HeapInteger(int m, boolean b){
            maxsize.set(m);
            big=b;
    }
    public HeapInteger(int m){
        maxsize.set(m);
    }
    public HeapInteger(boolean b){
        big = b;
    }
    public HeapInteger(){

    }
    public Map insert(Integer key,T data){
        T tk=null;
        int bigtag=-1;
        if(big)
            bigtag=1;
        if(size.get()> maxsize.get()){
            if(key.compareTo(Heaparr.get(size.get()-1).getKey())==bigtag)
                return null;
            tk=Heaparr.get(size.get()-1).getValue();
            Heaparr.remove(size.get()-1);
            size.decrementAndGet();
        }
        int index=size.get();
        Entry info=new Entry() {
            @Override
            public Integer getKey() {
                return key;
            }

            @Override
            public T getValue() {
                return data;
            }
        };
        Heaparr.add(size.get(),info);

        while(Heaparr.get(index/2).getKey().compareTo(key)==bigtag){
            Entry temp=Heaparr.get(index/2);
            Heaparr.set(index/2,info);
            Heaparr.set(index,temp);
            index/=2;
        }
        final Integer lastindex=index;
        final T lastdata=data;
        size.getAndIncrement();
        return null;
    }
    public Map<T, Integer> deleteMinOrMax(){
        if(size.get()==0)
            return null;
        T tk=null;
        int bigtag=-1;
        if(big)
            bigtag=1;
        tk=Heaparr.get(0).getValue();
        Integer val=Heaparr.get(0).getKey();
        Map res=new HashMap();
        res.put(tk,val);
        Heaparr.set(0,Heaparr.get(size.get()-1));
        Heaparr.remove(size.decrementAndGet());
        int father=0;
        for (int child=father*2+1; child < size.get();child = 2*father+1 ){
            if (child<size.get()-1 && Heaparr.get(child).getKey().compareTo(Heaparr.get(child+1).getKey())==bigtag) {
                child++;
            }
            if (Heaparr.get(father).getKey().compareTo(Heaparr.get(child).getKey())==bigtag) {
                Entry temp = Heaparr.get(father);
                Heaparr.set(father,Heaparr.get(child));
                Heaparr.set(child,temp);
                father =child;
            }else {
                break;
            }
        }
        return res;
    }

    @Override
    public Map<T,Integer> findHighest() {
        if(Heaparr.isEmpty())
            return null;
        T tk=Heaparr.get(0).getValue();
        Integer val=Heaparr.get(0).getKey();
        Map res=new HashMap();
        res.put(tk,val);
        return res;
    }

    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get()==0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Integer integer) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return Heaparr.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return Heaparr.retainAll(c);
    }

    @Override
    public void clear() {
        Heaparr.clear();
    }

    @Override
    public List getList() {
        return Heaparr;
    }

    public Map<T, Integer> delete(int index){
        if(index>size.get())
            return null;
        T tk=null;
        int bigtag=-1;
        if(big)
            bigtag=1;

        tk=Heaparr.get(index).getValue();
        Integer val=Heaparr.get(index).getKey();
        Map res=new HashMap();
        res.put(tk,val);
        Heaparr.set(index,Heaparr.get(size.get()-1));

        Heaparr.remove(size.decrementAndGet());
        int father=index;
        for (int child=father*2+1; child < size.get();child = 2*father+1 ){
            if (child!=size.get()-1 && Heaparr.get(child).getKey().compareTo(Heaparr.get(child+1).getKey())==bigtag ) {
                child++;
            }
            if (Heaparr.get(child).getKey().compareTo(Heaparr.get(child+1).getKey())==bigtag) {
                Entry temp = Heaparr.get(father);
                Heaparr.set(father,Heaparr.get(child));
                Heaparr.set(child,temp);
                father =child;
            }else {
                break;
            }
        }
        return res;
    }
}
