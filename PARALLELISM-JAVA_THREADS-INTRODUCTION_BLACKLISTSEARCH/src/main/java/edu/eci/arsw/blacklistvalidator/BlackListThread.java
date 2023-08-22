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
