package data_sturcts;

import ArrivalBuffer;
import Parcel;
import java.util.logging.*;


public class DestinationSorter {
    private static final Logger logger = Logger.getLogger(DestinationSorter.class.getName());

    private class Node {
        String cityName;
        ArrivalBuffer parcelList;
        Node left, right;

        Node(String cityName) {
            this.cityName = cityName;
            this.parcelList = new ArrivalBuffer(Integer.MAX_VALUE); // max as def cap value::
            this.left = null;
            this.right = null;
        }
    }

    private Node root;
    //variarble for logging::
    private int totalParcelsSorted;
    private int totalParcelsDispatched;
    
    public DestinationSorter() {
        root = null;
        totalParcelsSorted = 0;
        totalParcelsDispatched = 0;
    }

    // 🟢 Parcel ekleme
    public void insertParcel(Parcel parcel) {
        root = insertParcelRecursive(root, parcel);
        totalParcelsSorted++;
        logger.info(String.format("[Sort] Parcel %s sorted to %s (Priority: %d)", 
            parcel.getParcelID(), parcel.getDestinationCity(), parcel.getPriority()));
    }

    private Node insertParcelRecursive(Node node, Parcel parcel) {
        if (node == null) {
            Node newNode = new Node(parcel.getDestinationCity());
            newNode.parcelList.enqueue(parcel);

            logger.info(String.format("[New City] Created node for %s", parcel.getDestinationCity()));

            return newNode;
        }

        int compare = parcel.getDestinationCity().compareToIgnoreCase(node.cityName);
        if (compare < 0) {
            node.left = insertParcelRecursive(node.left, parcel);
        } else if (compare > 0) {
            node.right = insertParcelRecursive(node.right, parcel);
        } else {
            // Aynı şehirse kuyruğa ekle
            node.parcelList.enqueue(parcel);
        }
        return node;
    }

    // 🟡 Belirli bir şehir için kuyruktaki tüm kargoları al
    public ArrivalBuffer getCityParcels(String city) {
        Node node = search(root, city);
        if (node != null) {
            logger.info(String.format("[City Status] %s has %d parcels in queue", 
                city, node.parcelList.size()));
        }
        return (node != null) ? node.parcelList : null;
    }

    private Node search(Node node, String city) {
        if (node == null) return null;
        int compare = city.compareToIgnoreCase(node.cityName);
        if (compare < 0) return search(node.left, city);
        else if (compare > 0) return search(node.right, city);
        else return node;
    }

    // 🔴 Belirli şehirden bir parcel sil (kargo gönderildikten sonra)
    public boolean removeParcel(String city, String parcelID) {
        Node node = search(root, city);
        if (node != null && !node.parcelList.isEmpty()) {
            // Create a temporary buffer to hold parcels
            ArrivalBuffer tempBuffer = new ArrivalBuffer(Integer.MAX_VALUE);
            boolean found = false;
            
            // Move all parcels except the one to remove to temp buffer
            while (!node.parcelList.isEmpty()) {
                Parcel p = node.parcelList.dequeue();
                if (!p.getParcelID().equals(parcelID)) {
                    tempBuffer.enqueue(p);
                } else {
                    found = true;

                    totalParcelsDispatched++;
                    logger.info(String.format("[Dispatch] Parcel %s dispatched from %s", 
                        parcelID, city));
                }
            }
            
            // Move parcels back to original buffer
            while (!tempBuffer.isEmpty()) {
                node.parcelList.enqueue(tempBuffer.dequeue());
            }
            
            return found;
        }
        return false;
    }

    // 🟢 Şehir adına göre alfabetik sıralı BST dolaşımı
    public void inOrderTraversal() {
        logger.info("\n===+ Current BST Status +===");
        inOrderRecursive(root);
        logger.info("===+ End BST Status +===\n");
    }

    private void inOrderRecursive(Node node) {
        if (node != null) {
            inOrderRecursive(node.left);
            logger.info(String.format("City: %s | Parcel Count: %d", 
                node.cityName, node.parcelList.size()));
            inOrderRecursive(node.right);
        }
    }

    // 🔍 Şehirde kaç kargo var?
    public int countCityParcels(String city) {
        Node node = search(root, city);
        int count = (node != null) ? node.parcelList.size() : 0;
        logger.info(String.format("[City Count] %s has %d parcels", city, count));
        return count;
    }

    // 🌳 BST yüksekliği
    public int getHeight() {
        logger.info(String.format("[BST Height] Current height: %d", calculateHeight(root)));
        return calculateHeight(root);
    }

    private int calculateHeight(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }

    // 📊 Toplam şehir (düğüm) sayısı
    public int getCityCount() {
        logger.info(String.format("[City Count] Total cities in BST: %d", countNodes(root);));
        return countNodes(root);;
    }

    private int countNodes(Node node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }

    // 🚩 En çok yüke sahip şehir (en fazla parcel içeren node)
    public String getBusiestCity() {
        //logging::
        if (busiest != null) {
            Node node = search(root, findMaxCity(root, null, 0) );
            logger.info(String.format("\n[Busiest City] %s with %d parcels", 
                findMaxCity(root, null, 0) , node.parcelList.size()) );
        }
        return findMaxCity(root, null, 0);;
    }

    private String findMaxCity(Node node, String maxCity, int maxCount) {
        if (node == null) return maxCity;
        if (node.parcelList.size() > maxCount) {
            maxCity = node.cityName;
            maxCount = node.parcelList.size();
        }
        maxCity = findMaxCity(node.left, maxCity, maxCount);
        maxCity = findMaxCity(node.right, maxCity, maxCount);
        return maxCity;
    }

    //  Get logging statistics::
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("\n===+ DestinationSorter Statistics +===\n");
        stats.append(String.format("Total Parcels Sorted: %d\n", totalParcelsSorted));
        stats.append(String.format("Total Parcels Dispatched: %d\n", totalParcelsDispatched));
        stats.append(String.format("BST Height: %d\n", getHeight()));
        stats.append(String.format("Total Cities: %d\n", getCityCount()));
        stats.append(String.format("Busiest City: %s\n", getBusiestCity()));
        stats.append("===+ End Statistics +===\n");
        return stats.toString();
    }
}

