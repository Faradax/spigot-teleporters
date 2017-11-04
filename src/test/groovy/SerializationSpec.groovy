import de.faradax.minecraft.teleporters.Teleporter
import org.bukkit.Location
import org.bukkit.block.Block
import spock.lang.Specification

/**
 * Created by dockworker on 04.11.17.
 */
class SerializationSpec extends Specification {

    def "serializes one teleporter, then deserializes successfully" () {
        given: "One teleporter"
        def block = Stub(Block)
        def location = Stub(Location)
        location.block >> block
        block.location >> location
        Teleporter teleporter = new Teleporter(id: null, block: block)

        when: "serialization & deserializsation is called"
        def maps = [teleporter.serialize()]
        def result = Teleporter.deserialize(maps)[0]

        then: "teleporters are correctly configured"
        result.id == teleporter.id
        result.block == teleporter.block
        result.target == result
    }

    def "serializes two teleporters, then deserializes successfully" () {
        given: "One teleporter"
        Block block = makeBlock(1.0, 2.0, 3.0)
        Block block2 = makeBlock(5.0, 8.0, 9.0)
        Teleporter teleporter = new Teleporter(id: null, block: block)
        Teleporter teleporter2 = new Teleporter(id: null, block: block2)
        teleporter.target = teleporter2
        teleporter2.target = teleporter

        when: "serialization & deserializsation is called"
        def maps = [teleporter.serialize(), teleporter2.serialize()]
        def (result, result2) = Teleporter.deserialize(maps)

        then: "teleporters are correctly configured"
        result.target == result2
        result2.target == result
    }

    private Block makeBlock(BigDecimal x, BigDecimal y, BigDecimal z) {
        def block = Stub(Block)
        def location = Stub(Location)
        location.block >> block
        location.x >> x
        location.y >> y
        location.z >> z
        block.location >> location
        block
    }
}
