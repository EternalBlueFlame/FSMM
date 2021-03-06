package net.fexcraft.mod.lib.fcl;

import java.util.List;

import javax.annotation.Nullable;

/**
 * @author Ferdinand Calo' (FEX___96)
 *
 * General ArrayList which will not cause "out of bounds" exceptions, but instead return null or first entry.
 * imported/copied from FCL.
 * @param <T>
 */
public class ArrayList<T> extends java.util.ArrayList<T> {

    private static final long serialVersionUID = 6551014441630799597L;

    public ArrayList(List<T> list){
        super(list);
    }

    public ArrayList(){
        super();
    }

    public ArrayList(T[] arr){
        super();
        for(T e : arr){
            this.add(e);
        }
    }

    @Override @Nullable
    public T get(int i){
        return this.isEmpty() ? null : i > this.size() ? super.get(0) : i < 0 ? null : super.get(i);
    }

}
