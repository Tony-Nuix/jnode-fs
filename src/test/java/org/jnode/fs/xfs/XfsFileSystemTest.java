package org.jnode.fs.xfs;

import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.*;
import org.jnode.fs.service.FileSystemService;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XfsFileSystemTest {

    private FileSystemService fss;

    @Before
    public void setUp() {
        // create file system service.
        fss = FileSystemTestUtils.createFSService(XfsFileSystemType.class.getName());
    }

    @Test
    public void testImage1() throws Exception {
        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/test-xfs-1.img");

        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = fss.getFileSystemType(XfsFileSystemType.ID);
            XfsFileSystem fs = type.create(device, true);

            String expectedStructure = "type: XFS vol:\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 total:134217728 free:130490368\n" +
                    "  /; \n" +
                    "    folder1; \n" +
                    "      this_is_fine.jpg; 53072; ee04081c3182a44a1c6944e94012e977\n" +
                    "    folder 2; \n" +
                    "      xfs.zip; 20103; d5f8c07fdff365b45b8af1ae7622a98d\n" +
                    "    testfile.txt; 20; 5dd39cab1c53c2c77cd352983f9641e1\n";
            DataStructureAsserts.assertStructure(fs, expectedStructure);
        } finally {
            testFile.delete();
        }
    }


    @Test
    public void testXfsMetaData() throws Exception {
        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/test-xfs-1.img");

        // Arrange
        String expectedStructure =
                "  /; \n" +
                "    atime : 2021-11-17T00:50:04-0600; ctime : 2021-11-17T00:48:33-0600; mtime : 2021-11-17T00:48:33-0600\n" +
                "    owner : 0; group : 0; size : 57; mode : 777; \n" +
                "    folder1; \n" +
                "        atime : 2021-11-17T00:50:07-0600; ctime : 2021-11-17T00:50:07-0600; mtime : 2021-11-17T00:50:07-0600\n" +
                "        owner : 1000; group : 1000; size : 30; mode : 775; \n" +
                "      this_is_fine.jpg; \n" +
                "            atime : 2021-11-17T00:50:07-0600; ctime : 2021-11-17T00:50:07-0600; mtime : 2019-05-19T18:45:52-0500\n" +
                "            owner : 1000; group : 1000; size : 53072; mode : 744; \n" +
                "    folder 2; \n" +
                "        atime : 2021-11-17T00:52:07-0600; ctime : 2021-11-17T00:52:07-0600; mtime : 2021-11-17T00:52:07-0600\n" +
                "        owner : 1000; group : 1000; size : 21; mode : 775; \n" +
                "      xfs.zip; \n" +
                "            atime : 2021-11-17T00:52:07-0600; ctime : 2021-11-17T00:52:07-0600; mtime : 2021-11-17T00:52:03-0600\n" +
                "            owner : 1000; group : 1000; size : 20103; mode : 744; \n" +
                "    testfile.txt; \n" +
                "        atime : 2021-11-17T00:48:33-0600; ctime : 2021-11-17T00:48:33-0600; mtime : 2021-11-17T00:48:33-0600\n" +
                "        owner : 1000; group : 1000; size : 20; mode : 664; \n";

        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);

            FSEntry entry = fs.getRootEntry();
            StringBuilder actual = new StringBuilder(expectedStructure.length());

            DataStructureAsserts.buildXfsMetaDataStructure(entry, actual, "  ");

            assertThat(actual.toString(), is(expectedStructure));

        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testCentos() throws Exception {

        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/centos-xfs.img");

        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);
            FSEntry entry = fs.getRootEntry();
            StringBuilder actual = new StringBuilder(1024*1024);
            buildXfsDirStructure(entry, actual, "  ");
            //System.out.println(actual);
        } finally {
            testFile.delete();
        }
    }


    public static void buildXfsDirStructure(FSEntry entry,StringBuilder actual, String indent) throws IOException
    {
        actual.append(indent);
        actual.append(entry.getName());
        actual.append("; \n");
        System.out.println(actual);

        if (entry.isDirectory()) {
            FSDirectory directory = entry.getDirectory();

            Iterator<? extends FSEntry> iterator = directory.iterator();

            while (iterator.hasNext()) {
                FSEntry child = iterator.next();

                if (".".equals(child.getName()) || "..".equals(child.getName()))
                {
                    continue;
                }

                buildXfsDirStructure(child, actual, indent + "  ");
            }
        }
    }
}
