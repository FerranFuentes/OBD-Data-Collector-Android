package com.example.obd2pti.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.obd2pti.MainActivity
import com.example.obd2pti.R
import com.example.obd2pti.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private  lateinit var mainAct: MainActivity
    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var DiscoveredDevicesNames = ArrayList<String>()
    private var PairedDevicesNames = ArrayList<String>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Call getPairedDevices from MainActivity.kt

        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        mainAct = activity as MainActivity
        //val pairedBtn = rootView.findViewById<Button>(R.id.pairedbutton)
        //val discoverBtn = rootView.findViewById<Button>(R.id.discoverbutton)




        binding.pairedbutton.setOnClickListener {
            Toast.makeText(context, "Paired Devices", Toast.LENGTH_SHORT).show()
            PairedDevicesNames = mainAct.getPairedDevices()
            val listAdapter = ArrayAdapter<String>(mainAct, android.R.layout.simple_list_item_1, PairedDevicesNames)
            binding.listView.adapter = listAdapter
        }

        binding.discoverbutton.setOnClickListener {
            Toast.makeText(context, "Discover Devices", Toast.LENGTH_SHORT).show()
            DiscoveredDevicesNames= mainAct.findDevices()
            val listAdapter = ArrayAdapter<String>(mainAct, android.R.layout.simple_list_item_1, DiscoveredDevicesNames)
            binding.listView.adapter = listAdapter
            if (DiscoveredDevicesNames.isEmpty()){
                Toast.makeText(context, "No Devices Found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position) as String
            Toast.makeText(context, "Connecting to $item", Toast.LENGTH_SHORT).show()
            var connection_status = mainAct.connectToBLDevice(item)
            if (connection_status == 1) {
                Toast.makeText(context, "Connected to $item", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to connect to $item", Toast.LENGTH_SHORT).show()
            }
        }

        //Toast.makeText(context, "Hola Caracola", Toast.LENGTH_SHORT).show()


        return root
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}