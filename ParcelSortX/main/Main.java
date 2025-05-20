package main;

import data_sturcts.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Ayarları yüke
            ConfigManager config = new ConfigManager("ParcelSortX/config.txt");

            int maxTicks = config.getMaxTicks();
            int queueCapacity = config.getQueueCapacity();
            int terminalRotationInterval = config.getTerminalRotationInterval();
            int parcelMin = config.getParcelPerTickMin();
            int parcelMax = config.getParcelPerTickMax();
            double misroutingRate = config.getMisroutingRate();
            String[] cityList = config.getCityList();

            // 2. Yapıları başlat
            ParcelGenerator generator = new ParcelGenerator(cityList, parcelMin, parcelMax);
            ArrivalBuffer arrivalBuffer = new ArrivalBuffer(queueCapacity);
            DestinationSorter destinationSorter = new DestinationSorter();
            TerminalRotator terminalRotator = new TerminalRotator(terminalRotationInterval);
            terminalRotator.initializeFromCityList(cityList);
            ReturnStack returnStack = new ReturnStack();
            ParcelTracker parcelTracker = new ParcelTracker();

            FileWriter logWriter = new FileWriter("log.txt");

            int tick = 0;
            int maxQueueSize = 0;
            int maxStackSize = 0;

            while (tick < maxTicks) {
                tick++;
                logWriter.write("[Tick " + tick + "]\n");

                // Yeni kargo oluştur
                Parcel[] newParcels = generator.generateParcelsForTick(tick);
                StringBuilder newParcelLog = new StringBuilder();
                List<String> sortedParcelIDs = new ArrayList<>();
                for (Parcel p : newParcels) {
                    boolean added = arrivalBuffer.enqueue(p);
                    if (added) {
                        parcelTracker.insert(p.getParcelID(), ParcelTracker.ParcelStatus.IN_QUEUE, p.getArrivalTick(),
                                p.getDestinationCity(), p.getPriority(), p.getSize());
                        newParcelLog.append(String.format("%s to %s (Priority %d), ", p.getParcelID(), p.getDestinationCity(), p.getPriority()));
                    }
                }
                if (newParcelLog.length() > 0) {
                    newParcelLog.setLength(newParcelLog.length() - 2); // Remove last comma
                    logWriter.write("New Parcels: " + newParcelLog + "\n");
                }

                maxQueueSize = Math.max(maxQueueSize, arrivalBuffer.size());
                logWriter.write("Queue Size: " + arrivalBuffer.size() + "\n");

                // Kuyruktan BST'ye aktar
                while (!arrivalBuffer.isEmpty()) {
                    Parcel p = arrivalBuffer.dequeue();
                    destinationSorter.insertParcel(p);
                    parcelTracker.updateStatus(p.getParcelID(), ParcelTracker.ParcelStatus.SORTED);
                    sortedParcelIDs.add(p.getParcelID());
                }
                if (!sortedParcelIDs.isEmpty()) {
                    logWriter.write("Sorted to BST: " + String.join(", ", sortedParcelIDs) + "\n");
                }

                // Aktif terminal ve dispatch
                String activeCity = terminalRotator.getActiveTerminal();
                Parcel nextParcel = destinationSorter.getNextParcelForCity(activeCity);
                if (nextParcel != null) {
                    boolean misrouted = Math.random() < misroutingRate;
                    if (misrouted) {
                        returnStack.push(nextParcel);
                        parcelTracker.updateStatus(nextParcel.getParcelID(), ParcelTracker.ParcelStatus.RETURNED);
                        parcelTracker.incrementReturnCount(nextParcel.getParcelID());
                        logWriter.write(String.format("Returned: %s misrouted -> Pushed to ReturnStack\n", nextParcel.getParcelID()));
                    } else {
                        destinationSorter.removeParcel(activeCity, nextParcel.getParcelID());
                        parcelTracker.updateStatus(nextParcel.getParcelID(), ParcelTracker.ParcelStatus.DISPATCHED);
                        logWriter.write(String.format("Dispatched: %s from BST to %s -> Success\n", nextParcel.getParcelID(), activeCity));
                    }
                }

                // ReturnStack yeniden işleme (her 3 tickte bir)
                if (tick % 3 == 0 && !returnStack.isEmpty()) {
                    Parcel returned = returnStack.pop();
                    destinationSorter.insertParcel(returned);
                    parcelTracker.updateStatus(returned.getParcelID(), ParcelTracker.ParcelStatus.SORTED);
                    logWriter.write("Reprocessed from ReturnStack: " + returned.getParcelID() + "\n");
                }

                maxStackSize = Math.max(maxStackSize, returnStack.size());

                // Terminal rotasyonu kontrolü
                String oldTerminal = activeCity;
                terminalRotator.updateTick(tick);
                String newTerminal = terminalRotator.getActiveTerminal();
                if (!oldTerminal.equals(newTerminal)) {
                    logWriter.write("Terminal Rotated to: " + newTerminal + "\n");
                }

                // Tick log üzeti
                logWriter.write("Active Terminal: " + newTerminal + "\n");
                logWriter.write("ReturnStack Size: " + returnStack.size() + "\n");

                for (String city : cityList) {
                    int count = destinationSorter.countCityParcels(city);
                    if (count > 0) {
                        logWriter.write(String.format("  %s: %d parcel(s)\n", city, count));
                    }
                }

                logWriter.write("-----------------------------\n");
            }

            logWriter.close();

            // Final raporu yaz (report.txt)
            FileWriter report = new FileWriter("report.txt");
            report.write("=== Simulation Report ===\n\n");
            report.write("1. Simulation Overview\n");
            report.write("------------------------\n");
            report.write("Total Ticks Executed: " + tick + "\n");
            report.write("Number of Parcels Generated: " + ParcelGenerator.getTotalGeneratedCount() + "\n\n");

            report.write("2. Parcel Statistics\n");
            report.write("------------------------\n");
            report.write("Total Dispatched Parcels: " + parcelTracker.countStatus(ParcelTracker.ParcelStatus.DISPATCHED) + "\n");
            report.write("Total Returned Parcels: " + parcelTracker.countStatus(ParcelTracker.ParcelStatus.RETURNED) + "\n");
            int inSystem = parcelTracker.countStatus(ParcelTracker.ParcelStatus.SORTED)
                          + parcelTracker.countStatus(ParcelTracker.ParcelStatus.IN_QUEUE);
            report.write("Parcels Still in Queue/BST/Stack: " + inSystem + "\n\n");

            report.write("3. Destination Metrics\n");
            report.write("------------------------\n");
            for (String city : cityList) {
                report.write(city + ": " + destinationSorter.totalDeliveredTo(city) + " parcels\n");
            }
            report.write("Most Frequently Targeted Destination: " + destinationSorter.getCityWithMaxParcels() + "\n\n");

            report.write("4. Timing and Delay Metrics\n");
            report.write("-----------------------------\n");
            report.write(parcelTracker.getTimingStats());

            report.write("5. Data Structure Statistics\n");
            report.write("-----------------------------\n");
            report.write("Maximum Queue Size Observed: " + maxQueueSize + "\n");
            report.write("Maximum Stack Size Observed: " + maxStackSize + "\n");
            report.write("Final Height of BST: " + destinationSorter.getHeight() + "\n");
            report.write("Hash Table Load Factor: " + parcelTracker.getLoadFactor() + "\n");

            report.close();
            System.out.println("Simulation completed. Report generated.");

        } catch (IOException e) {
            System.err.println("Failed to load config or write files: " + e.getMessage());
        }
    }
}
