package org.jnode.fs.xfs.directory;

import java.io.IOException;

import lombok.Getter;
import org.jnode.fs.xfs.XfsObject;

/**
 * Tree nodes, leaf and node directories, and leaf and node extended attributes use the xfs_da_blkinfo_t filesystem block header.
 * <pre>
 *     typedef struct xfs_da_blkinfo {
 *         __be32 forw;
 *         __be32 back;
 *         __be16 magic;
 *         __be16 pad;
 *     } xfs_da_blkinfo_t;
 * </pre>
 */
@Getter
public class DirectoryOrAttributeBlockInfo extends XfsObject {
    /**
     * The size of this class, it is sizeof(__be32 + __be32 + __be16 + __be16) = 12 bytes.
     */
    public static final int SIZE = 12;

    /**
     * The magic signature of a leaf directory entry v4.
     */
    private static final long LEAF_DIR_MAGIC = 0xD2F1;

    /**
     * The magic signature of the node directory entry.
     */
    private static final long NODE_DIR_MAGIC = 0xd2ff;

    /**
     * Logical block offset of the previous block at this level.
     */
    private final long forward;

    /**
     * The Logical block offset of the next block at this level.
     */
    private final long backward;

    /**
     * Magic number for this directory/attribute block
     */
    final long magic;

    /**
     * Creates a Leaf block information entry.
     *
     * @param data   of the inode.
     * @param offset of the inode's data
     * @throws IOException if an error occurs reading in the leaf directory.
     */
    public DirectoryOrAttributeBlockInfo(byte[] data, long offset) throws IOException {
        super(data, (int) offset);

        forward = readUInt32();
        backward = readUInt32();
        magic = readUInt16();

        checkSignature();

        //Padding to maintain alignment.
        skipBytes(2);
    }

    protected void checkSignature() throws IOException {
        if ((magic != LEAF_DIR_MAGIC) && (magic != NODE_DIR_MAGIC)) {
            throw new IOException("Wrong magic number for V2 XFS Leaf Dir or Node Dir Info: " + getAsciiSignature(magic));
        }
    }
}
