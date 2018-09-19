package fr.klemek.choices.dao;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;

import fr.klemek.choices.model.Room;
import fr.klemek.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class RoomDao {

    private static KeyFactory keyFactory;

    private RoomDao() {
    }

    private static KeyFactory getKeyFactory() {
        if (RoomDao.keyFactory == null)
            RoomDao.keyFactory = DaoUtils.getDatastore()
                    .newKeyFactory().setKind(Room.MODEL_KIND);// Is used for creating keys later
        return RoomDao.keyFactory;
    }

    //region Functions

    /**
     * Create a new room in the datastore.
     *
     * @param room the room to create
     * @return the new id created or null on error
     */
    @SuppressWarnings("unchecked")
    public static Long createRoom(Room room) {
        IncompleteKey key = RoomDao.getKeyFactory().newKey();
        FullEntity<IncompleteKey> incRoomEntity = (FullEntity<IncompleteKey>) DaoUtils
                .roomToEntityBuilder(room, Entity.newBuilder(key)).build();
        Entity roomEntity = DaoUtils.getDatastore().add(incRoomEntity); // Save the Entity
        room.setId(roomEntity.getKey().getId());
        Logger.log(Level.INFO, "Room {0} created : {1}", room.getSimpleId(), room);
        return roomEntity.getKey().getId();
    }

    /**
     * Get a room by its simple id from the datastore.
     *
     * @param simpleId the simple id of the room
     * @return the found room or null
     */
    public static Room getRoomBySimpleId(String simpleId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(Room.MODEL_KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(Room.KEY_SIMPLEID, simpleId))
                .setLimit(1)
                .build();
        QueryResults<Entity> room = DaoUtils.getDatastore().run(query);
        return room.hasNext() ? DaoUtils.entityToRoom(room.next()) : null;
    }

    /**
     * Update a room in the datastore.
     *
     * @param room the room to update
     */
    public static void updateRoom(Room room) {
        Key key = RoomDao.getKeyFactory().newKey(room.getId());
        Entity entity = (Entity) DaoUtils.roomToEntityBuilder(room, Entity.newBuilder(key)).build();
        DaoUtils.getDatastore().update(entity);
        Logger.log(Level.INFO, "Room {0} updated : {1}", room.getSimpleId(), room);
    }

    /**
     * Delete a room from the datastore.
     *
     * @param room the room to delete
     */
    public static void deleteRoom(Room room) {
        Key key = RoomDao.getKeyFactory().newKey(room.getId());
        DaoUtils.getDatastore().delete(key);
        Logger.log(Level.INFO, "Room {0} deleted", room.getSimpleId());
    }

    /**
     * List all active rooms simple ids.
     *
     * @return a list of Strings
     */
    public static List<String> listRoomSimpleIds() {
        List<String> output = new ArrayList<>();
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(Room.MODEL_KIND)
                .build();
        QueryResults<Entity> resultList = DaoUtils.getDatastore().run(query);
        Room room;
        while (resultList.hasNext()) {
            room = DaoUtils.entityToRoom(resultList.next());
            output.add(room.getSimpleId());
        }
        return output;
    }

    /**
     * Delete all rooms in the datastore.
     */
    public static void deleteAllRooms() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(Room.MODEL_KIND)
                .build();
        QueryResults<Entity> resultList = DaoUtils.getDatastore().run(query);
        int n = 0;
        while (resultList.hasNext()) {
            DaoUtils.getDatastore().delete(resultList.next().getKey());
            n++;
        }
        Logger.log(Level.INFO, "{0} rooms deleted", n);
    }

    //endregion

}
