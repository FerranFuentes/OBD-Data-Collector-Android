package com.example.obd2pti
import com.example.obd2pti.Datos
import com.example.obd2pti.MainActivity
import androidx.fragment.app.Fragment
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.util.JsonWriter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.github.pires.obd.*
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.engine.LoadCommand
import com.github.pires.obd.commands.engine.OilTempCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.engine.ThrottlePositionCommand
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand
import com.github.pires.obd.commands.fuel.FuelLevelCommand
import com.github.pires.obd.commands.protocol.EchoOffCommand
import com.github.pires.obd.commands.protocol.LineFeedOffCommand
import com.github.pires.obd.commands.protocol.SelectProtocolCommand
import com.github.pires.obd.commands.protocol.TimeoutCommand
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime

class OBD2Recoletion(): Thread() {

    //private var recolección = false
    lateinit var socket: BluetoothSocket
    var recoleccion = false
    lateinit var path:File

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run() {
        val letDirectory = File(path, "EXPORTS")
        if (!letDirectory.exists()) {
            letDirectory.mkdir()
        }
        //Get a string with the current date
        val today = LocalDate.now().toString()
        val file = File(letDirectory, "${today}.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        //delete file content
        file.writeText("")
        val fileWriter = FileWriter(file, true)
        val jsonWriter = JsonWriter(fileWriter)

        val listaDatos: MutableList<Datos> = mutableListOf()

        try {
            EchoOffCommand().run(socket.inputStream, socket.outputStream)
            LineFeedOffCommand().run(socket.inputStream, socket.outputStream)
            TimeoutCommand(500).run(socket.inputStream, socket.outputStream)
            SelectProtocolCommand(com.github.pires.obd.enums.ObdProtocols.AUTO).run(socket.inputStream, socket.outputStream)
            AmbientAirTemperatureCommand().run(
                socket.inputStream,
                socket.outputStream
            )
            recoleccion = true



        } catch (e: java.lang.Exception) {
            // handle errors
        }
        var speedcomm:SpeedCommand = SpeedCommand()
        var rpmcomm:RPMCommand = RPMCommand()
        var throttlecomm:ThrottlePositionCommand = ThrottlePositionCommand()
        var engineloadcomm:LoadCommand = LoadCommand()
        var coolanttempcomm:EngineCoolantTemperatureCommand = EngineCoolantTemperatureCommand()
        var oiltempcomm:OilTempCommand = OilTempCommand()
        var fuellevelcomm:FuelLevelCommand = FuelLevelCommand()
        var fuelconsumptioncomm: ConsumptionRateCommand = ConsumptionRateCommand()

        var datos0 = Datos()
        rpmcomm.run(socket.inputStream, socket.outputStream)
        speedcomm.run(socket.inputStream, socket.outputStream)
        val current0 = LocalDateTime.now()

        datos0.currentTime = current0.toString()
        datos0.speed = speedcomm.metricSpeed
        datos0.rpm = rpmcomm.rpm
        listaDatos.add(datos0)

        var queryNum = 0
        recoleccion = true
        while (recoleccion) {
            // Toast.makeText(this, "Query: $queryNum", Toast.LENGTH_SHORT).show()
            var datos = Datos()
            rpmcomm.run(socket.inputStream, socket.outputStream)
            speedcomm.run(socket.inputStream, socket.outputStream)
            //throttlecomm.run(socket.inputStream, socket.outputStream)
            //engineloadcomm.run(socket.inputStream, socket.outputStream)
            //coolanttempcomm.run(socket.inputStream, socket.outputStream)
            //oiltempcomm.run(socket.inputStream, socket.outputStream)
            //fuellevelcomm.run(socket.inputStream, socket.outputStream)
            //fuelconsumptioncomm.run(socket.inputStream, socket.outputStream)
            val current = LocalDateTime.now()

            datos.currentTime = current.toString()
            datos.speed = speedcomm.metricSpeed
            datos.rpm = rpmcomm.rpm
            //   datos.throttlePosition = throttlecomm.percentage
            //  datos.engineLoad = engineloadcomm.percentage
            // datos.coolantTemp = coolanttempcomm.temperature
            // datos.oilTemp = oiltempcomm.temperature
            // datos.fuelLevel = fuellevelcomm.fuelLevel
            // datos.fuelConsumption = fuelconsumptioncomm.litersPerHour
            listaDatos.add(datos)
            ++queryNum
            Thread.sleep(250)
        }
        jsonWriter.beginArray()
        listaDatos.forEach() {
            jsonWriter.beginObject()
            jsonWriter.name("time").value(it.currentTime)
            jsonWriter.name("speed").value(it.speed)
            jsonWriter.name("rpm").value(it.rpm)
            jsonWriter.name("throttle").value(it.throttlePosition)
            jsonWriter.name("engine_load").value(it.engineLoad)
            jsonWriter.name("engine_coolant_temp").value(it.coolantTemp)
            jsonWriter.name("oil_temp").value(it.oilTemp)
            jsonWriter.name("fuel_level").value(it.fuelLevel)
            jsonWriter.name("fuel_consumption").value(it.fuelConsumption)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()
        jsonWriter.close()
        fileWriter.close()
        return
    }

}
