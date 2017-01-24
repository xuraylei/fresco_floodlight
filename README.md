FRESCO(Floodlight) version 0.1
====================================
------------------------------------
##Description

FRESCO is an SDN development framework to facilitate development and deployment of network (security) services. FRESCO Floodlight version (or FRESCO_Floodlight) is an FRESCO implementation running on Floodlight SDN controller (1.1 or higher version), which is written in JAVA language. 


-----------------------------------
##Required Software
- Java 7 for Floodlight v1.2
- Floodlight v1.2 (built-in)

----------------------------------
##How to Install?
1. install essential software: `$ sudo apt-get install build-essential openjdk-7-jdk ant maven python-dev eclipse`
2. fork FRESCO_Floodlight repo: `$ git clone https://github.com/xuraylei/fresco_floodlight.git`

----------------------------------
##How to build your own network (security) services?
By default, FRESCO_floodlight has several sample network service/application in folder `freso_apps`, you can enable your service by putting them in `freso_apps/enable/`
For more information about how to write network service and existing FRESCO modoules, you can visit http://success.cse.tamu.edu/fresco/.

----------------------------------
##How to Use?
1. build FRESCO_Floodlight : `$ cd fresco_floodlight` and then use `$ ant `
2. run FRESCO_Floodlight with command `$ java -jar target/floodlight.jar`

-----------------------------------
##Contributors
Lei Xu       @       Texas A&M University

Guofei Gu      @     Texas A&M University

Robert A. Baykov   @ University of Maryland

Phillip Porras   @   SRI International

Seungwon Shin   @    Kaist University

------------------------------------
##Relevant Publication

Seungwon Shin, Phillip Porras, Vinod Yegneswaran, Martin Fong, Guofei Gu, and Mabry Tyson. "FRESCO: Modular Composable Security Services for Software-Defined Networks." In Proceedings of the 20th Annual Network & Distributed System Security Symposium (NDSS'13), San Diego, CA, USA, February 2013.

------------------------------------
##Contact Information


If you find any bug in current FRESCO(Floodlight) implementation, feel free to contact:

Lei Xu 

xray2012@tamu.edu
