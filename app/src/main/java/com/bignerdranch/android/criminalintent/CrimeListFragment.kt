package com.bignerdranch.android.criminalintent

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

private const val SAVED_SUBTITLE_VISIBLE = "subtitle"

class CrimeListFragment : androidx.fragment.app.Fragment() {

    private lateinit var crimeRecyclerView: androidx.recyclerview.widget.RecyclerView
    private var adapter: CrimeAdapter? = null
    private var subtitleVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView =
                view.findViewById(R.id.crime_recycler_view) as androidx.recyclerview.widget.RecyclerView
        crimeRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        subtitleVisible =
                savedInstanceState?.getBoolean(SAVED_SUBTITLE_VISIBLE) ?: false

        updateUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, subtitleVisible)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)

        val subtitleItem = menu?.findItem(R.id.show_subtitle)
        subtitleItem?.title = if (subtitleVisible) {
            getString(R.string.hide_subtitle)
        } else {
            getString(R.string.show_subtitle)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                CrimeLab.get().addCrime(crime)
                val intent = CrimePagerActivity.newIntent(requireContext(), crime.id)
                startActivity(intent)
                return true
            }
            R.id.show_subtitle -> {
                subtitleVisible = !subtitleVisible
                activity?.invalidateOptionsMenu()
                updateSubtitle()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateSubtitle() {
        val crimeLab = CrimeLab.get()
        val crimeCount = crimeLab.getCrimes().size
        var subtitle = getString(R.string.subtitle_format, crimeCount)

        if (!subtitleVisible) {
            subtitle = ""
        }

        val activity = activity as AppCompatActivity
        activity.supportActionBar?.subtitle = subtitle
    }

    private fun updateUI() {
        val crimeLab = CrimeLab.get()
        val crimes = crimeLab.getCrimes()

        adapter?.let {
            it.crimes = crimes
            it.notifyDataSetChanged()
        } ?: run {
            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
        }

        updateSubtitle()
    }

    private inner class CrimeHolder(view: View)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var crime: Crime

        private val titleTextView: TextView
        private val dateTextView: TextView
        private val solvedImageView: ImageView

        init {
            itemView.setOnClickListener(this)
            titleTextView = itemView.findViewById(R.id.crime_title)
            dateTextView = itemView.findViewById(R.id.crime_date)
            solvedImageView = itemView.findViewById(R.id.crime_solved)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            val intent = CrimePagerActivity.newIntent(requireContext(), crime.id)
            startActivity(intent)
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>)
        :  androidx.recyclerview.widget.RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemCount() = crimes.size
    }
}