package com.bignerdranch.android.criminalintent

import androidx.fragment.app.Fragment

class CrimeListActivity : SingleFragmentActivity() {

    override fun createFragment(): androidx.fragment.app.Fragment {
        return CrimeListFragment()
    }
}