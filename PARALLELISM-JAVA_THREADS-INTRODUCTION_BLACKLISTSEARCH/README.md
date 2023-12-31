
### Escuela Colombiana de Ingeniería
### Arquitecturas de Software - ARSW
## Ejercicio Introducción al paralelismo - Hilos - Caso BlackListSearch


## ESTUDIANTES: Nicolás Castro Jaramillo y Sergio Andres Gonzales


### Dependencias:
####   Lecturas:
*  [Threads in Java](http://beginnersbook.com/2013/03/java-threads/)  (Hasta 'Ending Threads')
*  [Threads vs Processes]( http://cs-fundamentals.com/tech-interview/java/differences-between-thread-and-process-in-java.php)

### Descripción
  Este ejercicio contiene una introducción a la programación con hilos en Java, además de la aplicación a un caso concreto.
  

**Parte I - Introducción a Hilos en Java**

1. De acuerdo con lo revisado en las lecturas, complete las clases CountThread, para que las mismas definan el ciclo de vida de un hilo que imprima por pantalla los números entre A y B.
```
package edu.eci.arsw.threads;

/**
 *
 * @author hcadavid
 */
public class CountThread extends Thread{

    private int numA;

    private int numB;

 

    public CountThread(int numA, int numB, String name){

        super(name);

        this.numA = numA;

        this.numB = numB;

    }

   

     public void run(){

        for (int i = numA; i < numB ; i++)

        System.out.println(i + " " + getName());

        System.out.println("Termina thread " + getName());

    }

}

```

2. Complete el método __main__ de la clase CountMainThreads para que:
	1. Cree 3 hilos de tipo CountThread, asignándole al primero el intervalo [0..99], al segundo [99..199], y al tercero [200..299].
	2. Inicie los tres hilos con 'start()'.
	3. Ejecute y revise la salida por pantalla. 
	4. Cambie el incio con 'start()' por 'run()'. Cómo cambia la salida?, por qué?.
```
package edu.eci.arsw.threads;

/**
 *
 * @author hcadavid
 */
public class CountThreadsMain {
    
    public static void main(String a[]){
        Thread hilo1 = new Thread(new CountThread(0,100,"hilo1"));

 

        Thread hilo2 = new Thread(new CountThread(99,200,"hilo2"));

 

        Thread hilo3 = new Thread(new CountThread(199,300,"hilo3"));

 

        hilo1.start();

        hilo2.start();

        hilo3.start();

 

        hilo1.run();

        hilo2.run();

        hilo3.run();
    }
    
}

```


con start()


![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/f7fedbf7-df45-49b5-ae02-be4d9fab0a73)

con run()


![](https://github.com/cattus09/ARSW_lab_1/blob/main/PARALLELISM-JAVA_THREADS-INTRODUCTION_BLACKLISTSEARCH/FOTOS/ARSW%20IMG2.png?raw=true)


*con start() obtenemos una salida desordenada, mientras que con run() obtenemos una salida donde se muestra una secuencia de números ordenadas e hilos desde el primero hasta el tercero, esto es debido a que start() los hilos se ejecutan en paralelo y cada hilo compite por el procesamiento de la CPU, lo que causa que cada vez que ejecutemos el programa, este tendrá una salida diferente, por lo que es un sistema no determinista, mientras que con run(), los hilos se ejecutan en secuencia según su orden en el metodo main(), por lo que no hay concurrencia y cada hilo se ejecuta secuencialmente.*


**Parte II - Ejercicio Black List Search**


Para un software de vigilancia automática de seguridad informática se está desarrollando un componente encargado de validar las direcciones IP en varios miles de listas negras (de host maliciosos) conocidas, y reportar aquellas que existan en al menos cinco de dichas listas. 

Dicho componente está diseñado de acuerdo con el siguiente diagrama, donde:

- HostBlackListsDataSourceFacade es una clase que ofrece una 'fachada' para realizar consultas en cualquiera de las N listas negras registradas (método 'isInBlacklistServer'), y que permite también hacer un reporte a una base de datos local de cuando una dirección IP se considera peligrosa. Esta clase NO ES MODIFICABLE, pero se sabe que es 'Thread-Safe'.

- HostBlackListsValidator es una clase que ofrece el método 'checkHost', el cual, a través de la clase 'HostBlackListDataSourceFacade', valida en cada una de las listas negras un host determinado. En dicho método está considerada la política de que al encontrarse un HOST en al menos cinco listas negras, el mismo será registrado como 'no confiable', o como 'confiable' en caso contrario. Adicionalmente, retornará la lista de los números de las 'listas negras' en donde se encontró registrado el HOST.

![](img/Model.png)

Al usarse el módulo, la evidencia de que se hizo el registro como 'confiable' o 'no confiable' se dá por lo mensajes de LOGs:

INFO: HOST 205.24.34.55 Reported as trustworthy

INFO: HOST 205.24.34.55 Reported as NOT trustworthy


Al programa de prueba provisto (Main), le toma sólo algunos segundos análizar y reportar la dirección provista (200.24.34.55), ya que la misma está registrada más de cinco veces en los primeros servidores, por lo que no requiere recorrerlos todos. Sin embargo, hacer la búsqueda en casos donde NO hay reportes, o donde los mismos están dispersos en las miles de listas negras, toma bastante tiempo.

Éste, como cualquier método de búsqueda, puede verse como un problema [vergonzosamente paralelo](https://en.wikipedia.org/wiki/Embarrassingly_parallel), ya que no existen dependencias entre una partición del problema y otra.

Para 'refactorizar' este código, y hacer que explote la capacidad multi-núcleo de la CPU del equipo, realice lo siguiente:

1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que haga la búsqueda de un segmento del conjunto de servidores disponibles. Agregue a dicha clase un método que permita 'preguntarle' a las instancias del mismo (los hilos) cuantas ocurrencias de servidores maliciosos ha encontrado o encontró.


```
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;


public class BlackListThread extends Thread {

    private final int inicio;
    private final int destino;
    private final String ip;
    private int ocurrencesCount;
    private HostBlacklistsDataSourceFacade skds;
    private LinkedList<Integer> blackListOcurrences;
    private static final int BLACK_LIST_ALARM_COUNT = 5;

    public BlackListThread(int inicio, int destino, String ip) {
        // buscar en un segmento específico de servidores utilizando el rango de índices 
        this.inicio = inicio;
        this.destino = destino;
        this.ip = ip;
        this.ocurrencesCount = 0;
        this.skds = HostBlacklistsDataSourceFacade.getInstance();
        this.blackListOcurrences = new LinkedList<>();
    }

    public void run() {
        
        // si el numero de ocurrencias es mayor a 5, es no confiable
        for (int i = inicio; i < destino && ocurrencesCount < BLACK_LIST_ALARM_COUNT ; i++) {
            if (skds.isInBlackListServer(i, ip)) {
                blackListOcurrences.add(i);
                ocurrencesCount++;
            }
        }
    }

    public int getOccurrencesCount() {
        return ocurrencesCount;
    }

}
```

2. Agregue al método 'checkHost' un parámetro entero N, correspondiente al número de hilos entre los que se va a realizar la búsqueda (recuerde tener en cuenta si N es par o impar!). Modifique el código de este método para que divida el espacio de búsqueda entre las N partes indicadas, y paralelice la búsqueda a través de N hilos. Haga que dicha función espere hasta que los N hilos terminen de resolver su respectivo sub-problema, agregue las ocurrencias encontradas por cada hilo a la lista que retorna el método, y entonces calcule (sumando el total de ocurrencuas encontradas por cada hilo) si el número de ocurrencias es mayor o igual a _BLACK_LIST_ALARM_COUNT_. Si se da este caso, al final se DEBE reportar el host como confiable o no confiable, y mostrar el listado con los números de las listas negras respectivas. Para lograr este comportamiento de 'espera' revise el método [join](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html) del API de concurrencia de Java. Tenga también en cuenta:

	* Dentro del método checkHost Se debe mantener el LOG que informa, antes de retornar el resultado, el número de listas negras revisadas VS. el número de listas negras total (línea 60). Se debe garantizar que dicha información sea verídica bajo el nuevo esquema de procesamiento en paralelo planteado.

	* Se sabe que el HOST 202.24.34.55 está reportado en listas negras de una forma más dispersa, y que el host 212.24.24.55 NO está en ninguna lista negra.

```
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        
        int ocurrencesCount=0;
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        
        int checkedListsCount=0;
        
        for (int i=0;i<skds.getRegisteredServersCount() && ocurrencesCount<BLACK_LIST_ALARM_COUNT;i++){
            checkedListsCount++;
            
            if (skds.isInBlackListServer(i, ipaddress)){
                
                blackListOcurrences.add(i);
                
                ocurrencesCount++;
            }
        }
        
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    public List<Integer> checkHost(int n, String ipaddress){
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        ArrayList<BlackListThread> hilos = new ArrayList<>();
        int dimencion = skds.getRegisteredServersCount() / n;
        int faltantes = skds.getRegisteredServersCount() % n;
        int ocurrencesCount = 0;
        int checkedListsCount = 0;
        int inicio = 0;
        int fin = 0;

        for (int i = 0; i < n; i ++){
            fin += dimencion;
            BlackListThread blackList = new BlackListThread(inicio, fin, ipaddress);
            inicio = fin;
            hilos.add(blackList);
            blackList.start();
        }

        if(faltantes > 0){
            BlackListThread blackList = new BlackListThread(inicio, fin + faltantes, ipaddress);
            hilos.add(blackList);
            blackList.start();
        }

        for(BlackListThread h : hilos){
            try{
                h.join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        
        for (int i=0;i<skds.getRegisteredServersCount() && ocurrencesCount<BLACK_LIST_ALARM_COUNT;i++){
            checkedListsCount++;         
            if (skds.isInBlackListServer(i, ipaddress)){               
                blackListOcurrences.add(i);               
                ocurrencesCount++;
            }
        }

        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}

```


Ip encontrada en las blacklist
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/8bb17368-0688-4995-bb83-3d324d1343a5)


Ip no encontrada en las blacklist
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/bad5d2ce-21a7-407b-b7f2-6341f6b9ca13)



**Parte II.I Para discutir la próxima clase (NO para implementar aún)**

La estrategia de paralelismo antes implementada es ineficiente en ciertos casos, pues la búsqueda se sigue realizando aún cuando los N hilos (en su conjunto) ya hayan encontrado el número mínimo de ocurrencias requeridas para reportar al servidor como malicioso. Cómo se podría modificar la implementación para minimizar el número de consultas en estos casos?, qué elemento nuevo traería esto al problema?

**Parte III - Evaluación de Desempeño**

A partir de lo anterior, implemente la siguiente secuencia de experimentos para realizar las validación de direcciones IP dispersas (por ejemplo 202.24.34.55), tomando los tiempos de ejecución de los mismos (asegúrese de hacerlos en la misma máquina):

Nucleos:
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/d963557f-a44e-4483-b1d6-98710a1f68c8)


1. Un solo hilo.
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/c83e34f9-3efb-4063-9dca-9d81f724abf5)

2. Tantos hilos como núcleos de procesamiento (haga que el programa determine esto haciendo uso del [API Runtime]
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/6d1a77f3-867e-4b3a-ac66-246049146a35)

3. Tantos hilos como el doble de núcleos de procesamiento.
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/07c7df85-833d-4acf-8b97-53edbae13aff)

4. 50 hilos.
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/25cf8299-ae46-427f-946d-4ef66a8eab7e)

5. 100 hilos.
![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/9e9c5048-9a41-490e-ab86-cbd643ebb093)


Al iniciar el programa ejecute el monitor jVisualVM, y a medida que corran las pruebas, revise y anote el consumo de CPU y de memoria en cada caso. ![](img/jvisualvm.png)

Con lo anterior, y con los tiempos de ejecución dados, haga una gráfica de tiempo de solución vs. número de hilos. Analice y plantee hipótesis con su compañero para las siguientes preguntas (puede tener en cuenta lo reportado por jVisualVM):

![image](https://github.com/cattus09/ARSW_lab_1/assets/98556822/65ede9b1-60c6-4755-b89d-bbed64852500)


**Parte IV - Ejercicio Black List Search**

1. Según la [ley de Amdahls](https://www.pugetsystems.com/labs/articles/Estimating-CPU-Performance-using-Amdahls-Law-619/#WhatisAmdahlsLaw?):

	![](img/ahmdahls.png), donde _S(n)_ es el mejoramiento teórico del desempeño, _P_ la fracción paralelizable del algoritmo, y _n_ el número de hilos, a mayor _n_, mayor debería ser dicha mejora. Por qué el mejor desempeño no se logra con los 500 hilos?, cómo se compara este desempeño cuando se usan 200?. 

*Teniendo en cuenta las pruebas hechas con un gran número de hilos, que aunque existe una mejora en términos de rendimiento, se llegara a un punto en la que la mejora será cada vez más pequeña, debido a que la fracción P/n de la formula será cada vez más pequeña debido a que n será más grande, por lo que deducimos que mientras más cantidad de hilos llegaremos a un límite en cuanto a mejora de rendimiento. Al usar 200 hilos tenemos un mejor rendimiento que con 500 debido a que la fracción P/n será más grande en comparación si usamos 500 hilos. aunque el mejoramiento puede ser sutil debido a que la fórmula también depende de una variable P.*

2. Cómo se comporta la solución usando tantos hilos de procesamiento como núcleos comparado con el resultado de usar el doble de éste?.

*En teoría utilizar el doble de hilos que núcleos disminuirá el tiempo de ejecucion como observamos haciendo las pruebas. Pero en la mayoría de los casos la mejora no sería tan significativa debido al valor que pueda tomar P.*

3. De acuerdo con lo anterior, si para este problema en lugar de 100 hilos en una sola CPU se pudiera usar 1 hilo en cada una de 100 máquinas hipotéticas, la ley de Amdahls se aplicaría mejor?. Si en lugar de esto se usaran c hilos en 100/c máquinas distribuidas (siendo c es el número de núcleos de dichas máquinas), se mejoraría?. Explique su respuesta.

*Si tuviéramos 1 hilo corriendo en cada máquina, la ley de Amdahls aplicaría ya que n al ser igual al número de máquinas se tendría que la fracción P sería igual a 1, por lo que la mejora de rendimiento sería bastante alta*

*si tuviéramos c hilos corriendo en 100/c maquinas existiría una mejora de rendimiento que dependería de cómo se distribuye la carga de hilos entre las maquinas ya que según la ley de Amdhal la distribución de carga en múltiples maquinas aumenta el rendimiento*





