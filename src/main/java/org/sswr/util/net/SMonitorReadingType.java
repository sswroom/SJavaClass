package org.sswr.util.net;

public enum SMonitorReadingType
{
	UNKNOWN,
	TEMPERATURE, //Degree Celsius
	RHUMIDITY, //%
	DCVOLTAGE, //V
	LATITUDE, //Degree
	LONGITUDE, //Degree
	FREQUENCY, //Hz
	ONOFF, //1 = on, 0 = off
	POWER, //1 = on, 0 = off
	AIR_PM2_5, //ppm
	AIR_PM10, //ppm
	AIR_CO, //ppm
	AIR_CO2, //ppm
	AIR_HCHO, //ppm
	AIR_VOC, //ppm
	PARTICLE_0_3UM, //ppm
	PARTICLE_0_5UM, //ppm
	PARTICLE_1_0UM, //ppm
	PARTICLE_2_5UM, //ppm
	PARTICLE_5_0UM, //ppm
	PARTICLE_10UM, //ppm
	ALTITUDE, //meter
	DISTANCE, //meter
	AHUMIDITY, //Pa
	SYSTEMCURRENT, //A
	ENGINERPM,
	COUNT,
	ACTIVEPOWER, //W
	APPARENTPOWER, //VA
	REACTIVEPOWER, //VAr
	POWERFACTOR,
	PHASEANGLE, //degree
	IMPORTACTIVEENERGY, //kWh
	EXPORTACTIVEENERGY, //kWh
	TOTALACTIVEENERGY, //kWh
	IMPORTREACTIVEENERGY, //kVArh
	EXPORTREACTIVEENERGY, //kVArh
	TOTALREACTIVEENERGY, //kVArh
	SYSTEMVOLTAGE, //V
	ACFREQUENCY, //Hz
	RSSI //dBm
}
