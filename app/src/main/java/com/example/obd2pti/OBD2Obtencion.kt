package com.example.obd2pti
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.JsonWriter
import androidx.annotation.RequiresApi
import com.github.pires.obd.*
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.control.TroubleCodesCommand
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
        var matricula : String
        val matriculaFile = File(path, "matricula.txt")
       try {
              matricula = matriculaFile.readText()
         } catch (e:Exception) {
              matriculaFile.createNewFile()
              matriculaFile.writeText("matricula")
              matricula = matriculaFile.readText()
       }
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
        var troublecodescomm: TroubleCodesCommand = TroubleCodesCommand()

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
           try {
               rpmcomm.run(socket.inputStream, socket.outputStream)
           } catch (e: Exception) {
              // println("Error al leer RPM");
              }
            try {
                speedcomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer velocidad");
            }
            try {
                throttlecomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer throttle");
            }
            try {
                engineloadcomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer engine load");
            }
            try {
                coolanttempcomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer coolant temp");
            }
            try {
                oiltempcomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer oil temp");
            }
            try {
                fuellevelcomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer fuel level");
            }
            try {
                fuelconsumptioncomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer fuel consumption");
            }
            if (queryNum == 1 )try {
                troublecodescomm.run(socket.inputStream, socket.outputStream)
            } catch (e: Exception) {
                //println("Error al leer trouble codes");
            }
            val current = LocalDateTime.now()
            datos.matricula = matricula
            datos.currentTime = current.toString()
            datos.speed = speedcomm.metricSpeed
            datos.rpm = rpmcomm.rpm
            datos.throttlePosition = throttlecomm.percentage
            datos.engineLoad = engineloadcomm.percentage
            datos.coolantTemp = coolanttempcomm.temperature
            datos.oilTemp = oiltempcomm.temperature
            datos.fuelLevel = fuellevelcomm.fuelLevel
            datos.fuelConsumption = fuelconsumptioncomm.litersPerHour
            datos.troubleCodes = troublecodescomm.formattedResult
            listaDatos.add(datos)
            ++queryNum
            Thread.sleep(750)
        }
        jsonWriter.beginArray()
        listaDatos.forEach() {
            jsonWriter.beginObject()
            jsonWriter.name("matricula").value(it.matricula)
            jsonWriter.name("timestamp").value(it.currentTime)
            jsonWriter.name("trouble_codes").value(it.troubleCodes)
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
