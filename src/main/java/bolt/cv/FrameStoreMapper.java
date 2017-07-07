package bolt.cv;

import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.tuple.ITuple;
import utils.SerializableMat;

/**
 * Created by john on 3/7/17.
 */
public class FrameStoreMapper implements RedisStoreMapper {

    private RedisDataTypeDescription description;
    private final String hashKey = "frame";

    public FrameStoreMapper() {
        description = new RedisDataTypeDescription(RedisDataTypeDescription.RedisDataType.HASH, hashKey);
    }

    @Override
    public RedisDataTypeDescription getDataTypeDescription() {
        return description;
    }

    @Override
    public String getKeyFromTuple(ITuple iTuple) {
        return (String) iTuple.getValueByField("word");
    }

    @Override
    public String getValueFromTuple(ITuple iTuple) {
        return (String) iTuple.getValueByField("frame");
    }
}
