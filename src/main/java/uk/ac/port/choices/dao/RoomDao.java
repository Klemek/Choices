package uk.ac.port.choices.dao;

import com.google.cloud.datastore.*;
import uk.ac.port.choices.model.Room;
import uk.ac.port.choices.utils.Logger;

import java.util.logging.Level;

public class RoomDao {

    static final String KIND = "Room";
    private static KeyFactory keyFactory;

    private RoomDao() {
    }

    private static KeyFactory getKeyFactory() {
        if (RoomDao.keyFactory == null)
            RoomDao.keyFactory = DaoUtils.getDatastore().newKeyFactory().setKind(RoomDao.KIND);// Is used for creating keys later
        return RoomDao.keyFactory;
    }

    //region Functions

    @SuppressWarnings("unchecked")
    public static Long createRoom(Room room) {
        IncompleteKey key = RoomDao.getKeyFactory().newKey();
        FullEntity<IncompleteKey> incRoomEntity = (FullEntity<IncompleteKey>) DaoUtils.roomToEntityBuilder(room, Entity.newBuilder(key)).build();
        Entity roomEntity = DaoUtils.getDatastore().add(incRoomEntity); // Save the Entity
        room.setId(roomEntity.getKey().getId());
        Logger.log(Level.INFO, "Room {0} created : {1}", room.getSimpleId(), room);
        return roomEntity.getKey().getId();
    }

    public static Room getRoomBySimpleId(String simpleId) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(RoomDao.KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq(Room.SIMPLEID, simpleId))
                .setLimit(1)
                .build();
        QueryResults<Entity> room = DaoUtils.getDatastore().run(query);
        return room.hasNext() ? DaoUtils.entityToRoom(room.next()) : null;
    }

    public static void updateRoom(Room room) {
        Key key = RoomDao.getKeyFactory().newKey(room.getId());
        Entity entity = (Entity) DaoUtils.roomToEntityBuilder(room, Entity.newBuilder(key)).build();
        DaoUtils.getDatastore().update(entity);
        Logger.log(Level.INFO, "Room {0} updated : {1}", room.getSimpleId(), room);
    }

    public static void deleteRoom(Room room) {
        Key key = RoomDao.getKeyFactory().newKey(room.getId());
        DaoUtils.getDatastore().delete(key);
        Logger.log(Level.INFO, "Room {0} deleted", room.getSimpleId());
    }

    //endregion

}
