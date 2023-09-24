package paxel.lib;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("ClassEscapesDefinedScope")
public class ResultExample {

    final Random todaysEvents = new Random();
    final boolean loggedIn = todaysEvents.nextBoolean();
    final boolean networkReachable = todaysEvents.nextBoolean();
    final boolean serverUp = todaysEvents.nextBoolean();
    final boolean userExpired = todaysEvents.nextBoolean();
    final boolean nameIsFound = todaysEvents.nextBoolean();
    final boolean addressIsFound = todaysEvents.nextBoolean();
    final boolean licensePlateIsFound = todaysEvents.nextBoolean();
    final boolean networkCrash = todaysEvents.nextBoolean();
    final boolean bugInServer = todaysEvents.nextBoolean();
    final boolean alienAttack = todaysEvents.nextBoolean();

    /**
     * Example for Result in a non-trivial mock method.
     * The method calls a REST service to get the ID of a user.
     * The parameters are fuzzy filled fullName, address, and licensePlate.
     * That can be either empty, valid or unknown.
     * The server can be down.
     * The auth can be expired.
     * The network disconnected, etc.
     * <p>
     * The users' only interest is the ID and where it's from or why he couldn't get it.
     *
     * @param fullName the full name of the requested person
     * @param address the address of the requested person
     * @param licensePlate the license plate of the requested person
     * @return The Result containing an ID definition or the reason why there is none
     */
    public Result<IdDef, IOException> getId(String fullName, String address, String licensePlate) {
        // we start by making sure that the rest service is reachable and we're connected
        Result<Boolean, IOException> login = checkLoginToRestService();
        if (!login.isSuccess())
            // We map the reason to a more descriptive reason.
            // We could just keep the original IOException, but the caller doesn't necessary know something about DNS addresses.
            // He wants an ID and doesn't know we have to get that from the network, so we tell him
            return login.mapError(e -> new IOException("Could not login to ID Server", e));

        if (!login.getValue())
            // we got a result from the login check, and it says the server doesn't allow this user to log in
            return Result.err(new IOException("The ID server doesn't accept our login"));


        String idPerName = null;
        if (fullName != null) {
            // this part looks not so good in java, because we don't have the '?' operator
            Result<String, IOException> idPerNameResult = getIdByName(fullName);
            // something with the network failed, we cancel other calls
            if (!idPerNameResult.isSuccess()) {
                return idPerNameResult.mapError(e -> new IOException("While requesting ID by name", e));
            }
            idPerName = idPerNameResult.getValue();
        }
        String idPerAddress = null;
        if (address != null) {
            Result<String, IOException> idPerAddressResult = getIdByAddress(address);
            if (!idPerAddressResult.isSuccess()) {
                return idPerAddressResult.mapError(e -> new IOException("While requesting ID by address", e));
            }
            idPerAddress = idPerAddressResult.getValue();
        }

        // We have asked IDs for all data that we have and now have to decide for the ID to take.
        // The license plate is the strongest ID and will be used unless name and address IDs exist and are different,
        // so check for those first
        if (idPerName != null && Objects.equals(idPerName, idPerAddress))
            return Result.ok(new IdDef(idPerName, "by name and address"));

        // we need to check for license plate now
        String idPerLicensePlate = null;
        if (licensePlate != null) {
            Result<String, IOException> idPerLicensePlateResult = getIdByLicense(licensePlate);
            if (!idPerLicensePlateResult.isSuccess())
                return idPerLicensePlateResult.mapError(e -> new IOException("While requesting ID by license plate", e));
            idPerLicensePlate = idPerLicensePlateResult.getValue();
        }
        if (idPerLicensePlate != null)
            return Result.ok(new IdDef(idPerLicensePlate, "by license plate"));
        // address wins over name
        if (idPerAddress != null)
            return Result.ok(new IdDef(idPerAddress, "by address"));
        if (idPerName != null)
            return Result.ok(new IdDef(idPerName, "by name"));
        return Result.ok(new IdDef(null, "ID not found"));
    }

    // ##############################################################################
    // below are the different possible outcomes of communicating with a REST service
    // ##############################################################################

    private Result<String, IOException> getIdByName(String value) {
        if (networkCrash)
            return Result.err(new IOException("Network crash"));
        if (nameIsFound)
            return Result.ok("" + todaysEvents.nextDouble());
        return Result.ok(null);
    }

    private Result<String, IOException> getIdByAddress(String value) {
        if (bugInServer)
            return Result.err(new IOException("Server reply unintelligible"));
        if (addressIsFound)
            return Result.ok("" + todaysEvents.nextDouble());
        return Result.ok(null);
    }

    private Result<String, IOException> getIdByLicense(String value) {
        if (alienAttack)
            return Result.err(new IOException("Server got hit by a laser"));
        if (licensePlateIsFound)
            return Result.ok("" + todaysEvents.nextDouble());
        return Result.ok(null);
    }

    private Result<Boolean, IOException> checkLoginToRestService() {
        if (!loggedIn)
            return logIn();
        // assuming we have a validly logged in connection
        return Result.ok(true);
    }

    private Result<Boolean, IOException> logIn() {
        // simulating trying to open a socket
        if (!networkReachable)
            return Result.err(new IOException("Network not reachable"));
        // simulating communication to server
        if (!serverUp)
            return Result.err(new IOException("Server down"));
        // simulating auth issue
        if (userExpired)
            return Result.ok(false);
        return Result.ok(true);
    }

    private static class IdDef {
        final String id;
        final String source;

        public IdDef(String id, String source) {
            this.id = id;
            this.source = source;
        }
    }
}
