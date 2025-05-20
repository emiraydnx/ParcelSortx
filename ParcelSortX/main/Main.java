package main;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        try {
            ConfigManager config = new ConfigManager("ParcelSortX/config.txt");

            System.out.println("Loaded Config:");
            System.out.println("Max Ticks: " + config.getMaxTicks());
            System.out.println("City List: " + Arrays.toString(config.getCityList()));
            System.out.println("Queue Capacity: " + config.getQueueCapacity());

            // Örnek: ParcelGenerator oluşturma
            ParcelGenerator generator = new ParcelGenerator(
                    config.getCityList(),
                    config.getParcelPerTickMin(),
                    config.getParcelPerTickMax());

            // Tick 0 için örnek üretim:
            Parcel[] parcels = generator.generateParcelsForTick(0);
            for (Parcel p : parcels) {
                System.out.println(p);
            }

        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }
}
