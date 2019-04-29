package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import java.util.Date
import java.util.UUID

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1

class CrimeFragment : androidx.fragment.app.Fragment() {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button

    override fun onResume() {
        super.onResume()
        Log.d("1.1.0-ALPHA07", "${titleField.text} is RESUMED")
    }

    override fun onPause() {
        super.onPause()

        Log.d("1.1.0-ALPHA07", "${titleField.text} is PAUSED")

        CrimeLab.get().updateCrime(crime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crime = CrimeLab.get().getCrime(crimeId) ?: Crime()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

        val titleWatcher = object: TextWatcher {

            override fun beforeTextChanged(sequence: CharSequence?,
                                           start: Int,
                                           count: Int,
                                           after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(sequence: CharSequence?,
                                       start: Int,
                                       before: Int,
                                       count: Int) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        titleField.apply {
            setText(crime.title)
            addTextChangedListener(titleWatcher)
        }

        updateDate()
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                val fragmentManager = this@CrimeFragment.requireFragmentManager()
                show(fragmentManager, DIALOG_DATE)
            }
        }

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            if (crime.suspect.isNotEmpty()) {
                text = crime.suspect
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(pickContactIntent,
                            PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_DATE && data != null -> {
                val date = data
                        .getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
                crime.date = date
                updateDate()
            }

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data
                // Specify which fields you want your query to return
                // values for.
                val queryFields: Array<String> =
                        arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // Perform your query - the contactUri is like a "where"
                // clause here
                val cursor = requireActivity().contentResolver
                        .query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    // Double-check that you actually got results
                    if (it.count == 0) {
                        return
                    }

                    // Pull out the first column of the first row of data -
                    // that is your suspect's name.
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    suspectButton.text = suspect
                }
            }
        }
    }

    private fun updateDate() {
        dateButton.text = crime.date.toString()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateFormat = "EEE, MMM dd"
        val dateString = DateFormat.format(dateFormat, crime.date).toString()
        var suspect = if (crime.suspect.isEmpty()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        val report = getString(R.string.crime_report,
                crime.title, dateString, solvedString, suspect)
        return report
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }

            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}