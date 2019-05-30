package com.example.dnssdservice

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private var serviceRequest: WifiP2pDnsSdServiceRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, mainLooper, null)

        start_discover_services.setOnClickListener(this)
        stop_discover_services.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.start_discover_services -> onStartDiscoverServices()
            R.id.stop_discover_services -> onStopDiscoverServices()
        }
    }

    private fun onStartDiscoverServices() {
        if (serviceRequest != null) {
            Toast.makeText(this, "Already running. Please stop first", Toast.LENGTH_SHORT).show()
            return
        }

        setDiscoveryButtonsEnabled(false)
        wifiP2pManager.setDnsSdResponseListeners(channel,
                ServiceResponseListener, TxtRecordListener)

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance("_anyfi._tcp")
        wifiP2pManager.addServiceRequest(
                channel, serviceRequest, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                startDiscoverServices()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "Failed to add service request: $reason",
                        Toast.LENGTH_SHORT).show()
                setDiscoveryButtonsEnabled(true)
            }
        })
    }

    private fun startDiscoverServices() {
        wifiP2pManager.discoverServices(channel, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@MainActivity, "Service discovery has been started",
                        Toast.LENGTH_SHORT).show()
                setDiscoveryButtonsEnabled(true)
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "Failed to discover services: $reason",
                        Toast.LENGTH_SHORT).show()
                setDiscoveryButtonsEnabled(true)
            }
        })
    }

    private fun onStopDiscoverServices() {
        if (serviceRequest == null) {
            Toast.makeText(this, "Service discovery is not started yet", Toast.LENGTH_SHORT).show()
            return
        }

        setDiscoveryButtonsEnabled(false)
        wifiP2pManager.removeServiceRequest(
                channel, serviceRequest, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(this@MainActivity, "Service discovery has been stopped",
                        Toast.LENGTH_SHORT).show()
                setDiscoveryButtonsEnabled(true)
                serviceRequest = null
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "Failed to stop service discovery: $reason",
                        Toast.LENGTH_SHORT).show()
                setDiscoveryButtonsEnabled(true)
            }
        })
    }

    private fun setDiscoveryButtonsEnabled(isEnabled: Boolean) {
        start_discover_services.isEnabled = isEnabled
        stop_discover_services.isEnabled = isEnabled
    }

    object TxtRecordListener : WifiP2pManager.DnsSdTxtRecordListener {
        override fun onDnsSdTxtRecordAvailable(fullDomainName: String?,
                                               txtRecordMap: MutableMap<String, String>?,
                                               srcDevice: WifiP2pDevice?) {
            Log.d(TAG, "fullDomainName: $fullDomainName")
            Log.d(TAG, "srcDevice: $srcDevice")
        }
    }

    object ServiceResponseListener : WifiP2pManager.DnsSdServiceResponseListener {
        override fun onDnsSdServiceAvailable(instanceName: String?,
                                             registrationType: String?,
                                             srcDevice: WifiP2pDevice?) {
            Log.d(TAG, "instanceName: $instanceName")
            Log.d(TAG, "registrationType: $registrationType")
            Log.d(TAG, "srcDevice: $srcDevice")
        }
    }
}
