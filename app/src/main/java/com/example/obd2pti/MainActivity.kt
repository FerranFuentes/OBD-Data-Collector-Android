package com.example.obd2pti

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.obd2pti.databinding.ActivityMainBinding
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import java.io.InputStream
import android.bluetooth.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.security.Key
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.ArrayList

const val REQUEST_ENABLE_BT = 1;
val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2




class MainActivity : AppCompatActivity() {

    private  val DevicesNames = ArrayList<String>()
    val PairedDevices:MutableMap<String,BluetoothDevice> = mutableMapOf<String, BluetoothDevice>()
    val DiscoveredDevices:MutableMap<String,BluetoothDevice> = mutableMapOf<String, BluetoothDevice>()
    private val DiscoveredDevicesNames = ArrayList<String>()


    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }


    private lateinit var binding: ActivityMainBinding


    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun requestLocationPermission() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Enable access to Location")
        dialogBuilder.setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                "location access in order to scan for BLE devices.")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Enable", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
                requestPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
                requestPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    ENABLE_BLUETOOTH_REQUEST_CODE
                )
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("AlertDialogExample")
        // show alert dialog
        alert.show()
    }

    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    if (deviceName != null) {
                        DiscoveredDevicesNames.add(deviceName)
                        DiscoveredDevices[deviceName] = device
                        val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, DiscoveredDevicesNames)
                        val dashboardPanel = LayoutInflater.from(this@MainActivity).inflate(R.layout.fragment_dashboard, null)
                        val listView = dashboardPanel.findViewById<ListView>(R.id.listView)
                        listView.adapter = adapter
                    }
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


       val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /*val dashboardPanel = LayoutInflater.from(this).inflate(R.layout.fragment_dashboard, null)
        val arrayTest = ArrayList<String>()
        arrayTest.add("Hola")
        arrayTest.add("Adios")
        val DevicesListView = dashboardPanel.findViewById<ListView>(R.id.listView)
        val listAdapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayTest)
        DevicesListView.adapter = listAdapter */


    }

    //Función para encontrar, seleccioanr y conectar el dispositivo bluetooth OBD2
    fun BluetoothFun() {
       /* val isLocationPermissionGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
                requestLocationPermission()
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    val myNewVal = ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                    val myManifest = Manifest.permission.BLUETOOTH_SCAN
                    Log.d("console", "before crash")
                    Log.d("console", "Manifest.permission.BLUETOOTH_SCAN: $myManifest")
                    Log.d("console", "My val: $myNewVal")
                    Log.d(
                        "console",
                        "PackageManager.PERMISSION_GRANTED: ${PackageManager.PERMISSION_GRANTED}"
                    )
                    Log.d(
                        "console",
                        "PackageManager.PERMISSION_GRANTED: ${Manifest.permission.BLUETOOTH_SCAN}"
                    )


                    return
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            Toast.makeText(this@MainActivity, "Entrando de obtener paired devices", Toast.LENGTH_SHORT).show()

            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                //DevicesNames.add(deviceName)
                PairedDevices[deviceName] = device
                DevicesNames.add(deviceName)
            }
            Toast.makeText(this@MainActivity, "Saliendo de obtener paired devices", Toast.LENGTH_SHORT).show()
        val dashboardPanel = LayoutInflater.from(this).inflate(R.layout.fragment_dashboard, null)

            val DevicesListView = dashboardPanel.findViewById<ListView>(R.id.listView)
        val listAdapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, DevicesNames)
            DevicesListView.adapter = listAdapter

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)

            //Start discovery
            bluetoothAdapter?.startDiscovery()

            //Stop discovery after 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                bluetoothAdapter?.cancelDiscovery()
            }, 10000)

*/
        }

        public fun getPairedDevices(): ArrayList<String> {
            val isLocationPermissionGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
                    requestLocationPermission()
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        val myNewVal = ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                        val myManifest = Manifest.permission.BLUETOOTH_SCAN
                        Log.d("console", "before crash")
                        Log.d("console", "Manifest.permission.BLUETOOTH_SCAN: $myManifest")
                        Log.d("console", "My val: $myNewVal")
                        Log.d(
                            "console",
                            "PackageManager.PERMISSION_GRANTED: ${PackageManager.PERMISSION_GRANTED}"
                        )
                        Log.d(
                            "console",
                            "PackageManager.PERMISSION_GRANTED: ${Manifest.permission.BLUETOOTH_SCAN}"
                        )


                        return DevicesNames
                    }
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            }
            else{
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
            }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            Toast.makeText(this@MainActivity, "Entrando de obtener paired devices", Toast.LENGTH_SHORT).show()

            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                //DevicesNames.add(deviceName)
                PairedDevices[deviceName] = device
                DevicesNames.add(deviceName)
            }
            Toast.makeText(this@MainActivity, "Saliendo de obtener paired devices", Toast.LENGTH_SHORT).show()
           /* val dashboardPanel = LayoutInflater.from(this).inflate(R.layout.fragment_dashboard, null)

            val DevicesListView = dashboardPanel.findViewById<ListView>(R.id.listView)
            val listAdapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, DevicesNames)
            DevicesListView.adapter = listAdapter */
            return DevicesNames
        }

        public fun findDevices() {
            val isLocationPermissionGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
                    requestLocationPermission()
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        val myNewVal = ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                        val myManifest = Manifest.permission.BLUETOOTH_SCAN
                        Log.d("console", "before crash")
                        Log.d("console", "Manifest.permission.BLUETOOTH_SCAN: $myManifest")
                        Log.d("console", "My val: $myNewVal")
                        Log.d(
                            "console",
                            "PackageManager.PERMISSION_GRANTED: ${PackageManager.PERMISSION_GRANTED}"
                        )
                        Log.d(
                            "console",
                            "PackageManager.PERMISSION_GRANTED: ${Manifest.permission.BLUETOOTH_SCAN}"
                        )


                        return
                    }
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            }
            else{
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
            }
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)

            //Start discovery
            bluetoothAdapter?.startDiscovery()

            //Stop discovery after 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                bluetoothAdapter?.cancelDiscovery()
            }, 10000)
        }
    }
