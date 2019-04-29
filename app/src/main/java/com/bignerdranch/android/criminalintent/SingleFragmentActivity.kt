package com.bignerdranch.android.criminalintent

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity

abstract class SingleFragmentActivity : AppCompatActivity() {

    protected abstract fun createFragment(): androidx.fragment.app.Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val fm: androidx.fragment.app.FragmentManager = supportFragmentManager
        var fragment: androidx.fragment.app.Fragment? = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = createFragment()
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }
    }
}