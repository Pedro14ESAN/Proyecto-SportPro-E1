package com.example.projectsportpro2026_2.datos.sincronizacion

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/*
 * HU16 - Observa si el dispositivo tiene red.
 *
 * Emite true/false cada vez que cambia la conectividad.
 * Requiere el permiso ACCESS_NETWORK_STATE
 * (permiso normal, no se pide al usuario).
 */
class MonitorConexion(
    contexto: Context
) {

    private val administrador: ConnectivityManager =
        contexto.applicationContext
            .getSystemService(ConnectivityManager::class.java)

    private fun hayInternet(): Boolean {
        val red =
            administrador.activeNetwork
                ?: return false

        val capacidades =
            administrador.getNetworkCapabilities(red)
                ?: return false

        return capacidades.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }

    val conectado: Flow<Boolean> =
        callbackFlow {

            trySend(hayInternet())

            val callback =
                object : ConnectivityManager.NetworkCallback() {

                    override fun onAvailable(network: Network) {
                        trySend(true)
                    }

                    override fun onLost(network: Network) {
                        trySend(false)
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        trySend(
                            networkCapabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_INTERNET
                            )
                        )
                    }
                }

            administrador.registerDefaultNetworkCallback(callback)

            awaitClose {
                administrador.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
}
