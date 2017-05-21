package app.sayor.crimeapp.models;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sayor on 3/23/2017.
 */

// singleton arraylist for the whole app
public class CrimeList {
    private static CrimeList crimeList;
    private List<Crime> mCrimes;

    public CrimeList(Context context) {
        if(context!=null)
            mCrimes = new ArrayList<>();
    }

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrimes(int pos) {
        return mCrimes.get(pos);
    }

    public void addCrimes(Crime crime){
        mCrimes.add(crime);
    }

    public void addmCrimes(int pos, Crime crime){
        mCrimes.add(pos, crime);
    }

    public void addAllCrimes(List<Crime> bookList){
        mCrimes.addAll(bookList);
    }

    public void removeCrimes(int pos){
        mCrimes.remove(pos);
    }

    public static CrimeList get(Context context){
        if(crimeList == null){
            crimeList = new CrimeList(context);
        }
        return crimeList;
    }
}
