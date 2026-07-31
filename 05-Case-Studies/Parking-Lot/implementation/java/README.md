# Parking Lot Java demo

The linked [ParkingLot.java](ParkingLot.java) is the runnable reference behind this case study.
Read the overview and normal flow first; then use the source to see how the
classes enforce states, failures, and invariants. The command below compiles it
into a temporary output directory, so it does not add build artifacts here.

Requires JDK 17 or newer. From the repository root:

```sh
mkdir -p /tmp/software-engineering-java
javac -d /tmp/software-engineering-java 05-Case-Studies/Parking-Lot/implementation/java/ParkingLot.java
java -ea -cp /tmp/software-engineering-java ParkingLot
```

The `-ea` flag enables the demo's assertions. See [ParkingLot.java](ParkingLot.java).
