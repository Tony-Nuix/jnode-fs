package org.jnode.fs.xfs;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.DataStructureAsserts;
import org.jnode.fs.FileSystemTestUtils;
import org.jnode.fs.*;
import org.jnode.fs.service.FileSystemService;

import org.jnode.fs.xfs.inode.INode;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

            String expectedStructure = "type: XFS vol: total:134217728 free:130490368\n" +
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
                "    atime : 2021-11-17T06:50:04.416+0000; ctime : 2021-11-17T06:48:33.735+0000; mtime : 2021-11-17T06:48:33.735+0000\n" +
                "    owner : 0; group : 0; size : 57; mode : 777; \n" +
                "    folder1; \n" +
                "        atime : 2021-11-17T06:50:07.494+0000; ctime : 2021-11-17T06:50:07.430+0000; mtime : 2021-11-17T06:50:07.430+0000\n" +
                "        owner : 1000; group : 1000; size : 30; mode : 775; \n" +
                "      this_is_fine.jpg; \n" +
                "            atime : 2021-11-17T06:50:07.430+0000; ctime : 2021-11-17T06:50:07.462+0000; mtime : 2019-05-19T23:45:52.237+0000\n" +
                "            owner : 1000; group : 1000; size : 53072; mode : 744; \n" +
                "    folder 2; \n" +
                "        atime : 2021-11-17T06:52:07.433+0000; ctime : 2021-11-17T06:52:07.421+0000; mtime : 2021-11-17T06:52:07.421+0000\n" +
                "        owner : 1000; group : 1000; size : 21; mode : 775; \n" +
                "      xfs.zip; \n" +
                "            atime : 2021-11-17T06:52:07.421+0000; ctime : 2021-11-17T06:52:07.425+0000; mtime : 2021-11-17T06:52:03.068+0000\n" +
                "            owner : 1000; group : 1000; size : 20103; mode : 744; \n" +
                "    testfile.txt; \n" +
                "        atime : 2021-11-17T06:48:33.735+0000; ctime : 2021-11-17T06:48:33.735+0000; mtime : 2021-11-17T06:48:33.735+0000\n" +
                "        owner : 1000; group : 1000; size : 20; mode : 664; \n";

        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);

            XfsEntry entry = fs.getRootEntry();
            StringBuilder actual = new StringBuilder(expectedStructure.length());

            buildXfsMetaDataStructure(entry, actual, "  ");

            assertThat(actual.toString(), is(expectedStructure));

        } finally {
            testFile.delete();
        }
    }

    /**
     * Builds up the structure for the given file system entry to get the metadata.
     *
     * @param entry  the entry to process.
     * @param actual the string to append to.
     * @param indent the indent level.
     * @throws IOException if an error occurs.
     */
    private static void buildXfsMetaDataStructure(XfsEntry entry, StringBuilder actual, String indent) throws IOException
    {
        actual.append(indent);
        actual.append(entry.getName());
        actual.append("; \n");

        if (entry.isDirectory()) {
            getXfsMetadata(entry, actual, indent);
        }
        if (entry.isFile()) {
            FSFile file = entry.getFile();
            getXfsMetadata(entry, actual, indent);
        }
        else {
            FSDirectory directory = entry.getDirectory();

            Iterator<? extends FSEntry> iterator = directory.iterator();

            while (iterator.hasNext()) {
                FSEntry child = iterator.next();

                if (".".equals(child.getName()) || "..".equals(child.getName()))
                {
                    continue;
                }

                buildXfsMetaDataStructure((XfsEntry)child, actual, indent + "  ");
            }
        }
    }

    /**
     * Get the metadata.
     *
     * @param entry  the entry to process.
     * @param actual the string to append to.
     * @param indent the indent level.
     *
     */
    private static StringBuilder getXfsMetadata(XfsEntry entry, StringBuilder actual,String indent) throws IOException {
        actual.append(indent);
        actual.append(indent);
        actual.append("atime : " + getDate(entry.getLastAccessed()));
        actual.append("; ");
        actual.append("ctime : " + getDate(entry.getCreated()));
        actual.append("; ");
        actual.append("mtime : " + getDate(entry.getLastChanged()) +"\n" );
        actual.append(indent);
        actual.append(indent);
        actual.append("owner : " + entry.getINode().getUid() );
        actual.append("; ");
        actual.append("group : " + entry.getINode().getGid() );
        actual.append("; ");
        actual.append("size : " +  entry.getINode().getSize() );
        actual.append("; ");
        String mode = Integer.toOctalString(entry.getINode().getMode());
        actual.append("mode : " +  mode.substring(mode.length()-3));
        actual.append("; \n");

        return actual;
    }

    /**
     * Convert epoch to human-readable date.
     *
     * @param date  the epoch value.
     */
    private static String getDate(long date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(new java.util.Date(date));
    }

    @Test
    public void testShortFormAttribute() throws Exception {
        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/extended_attr.img");
        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);
            final INode shortAttributeINode = fs.getINode(11075L);
            assertThat(shortAttributeINode.getAttributesFormat(),is(1L));// Short form attribute format
            final List<FSAttribute> attributes = shortAttributeINode.getAttributes();
            assertThat(attributes,hasSize(1));
            final FSAttribute attribute = attributes.get(0);
            assertThat(attribute.getName(),is("selinux"));
            assertThat(attribute.getValue(),is("unconfined_u:object_r:unlabeled_t:s0"));
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testLeafAttributes() throws Exception {
        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/extended_attr.img");
        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);
            final INode leafAttributeINode = fs.getINode(11076L);
            // leaf/node form attribute format
            assertThat(leafAttributeINode.getAttributesFormat(),is(2L));
            // leaf only has 1 extent
            assertThat(leafAttributeINode.getAttributeExtentCount(),is(1));

            final List<FSAttribute> attributes = leafAttributeINode.getAttributes();
            assertThat(attributes,hasSize(31));
            assertThat(attributes,everyItem(getSampleAttributeMatcher()));
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testNodeAttributes() throws Exception {
        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/extended_attr.img");
        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);
            final INode leafAttributeINode = fs.getINode(11077L);
            // leaf/node form attribute format
            assertThat(leafAttributeINode.getAttributesFormat(),is(2L));
            // node has more than 1 extent
            assertThat(leafAttributeINode.getAttributeExtentCount(),greaterThan(1));

            final List<FSAttribute> attributes = leafAttributeINode.getAttributes();
            assertThat(attributes,hasSize(201));
            assertThat(attributes,everyItem(getSampleAttributeMatcher()));
        } finally {
            testFile.delete();
        }
    }

    private Matcher<FSAttribute> getSampleAttributeMatcher(){
        return new BaseMatcher<FSAttribute>() {
            private final Pattern namePattern = Pattern.compile("sample-attr([0-9]+)");
            private final Pattern valuePattern = Pattern.compile("sample-value([0-9]+)");

            @Override
            public boolean matches(Object o) {
                if (o instanceof FSAttribute){
                    final FSAttribute attr = (FSAttribute) o;
                    final String name = attr.getName();
                    final String value = attr.getValue();
                    if (name.equals("selinux")){
                        return value.equals("unconfined_u:object_r:unlabeled_t:s0");
                    }
                    final java.util.regex.Matcher nameMatcher = namePattern.matcher(name);
                    final java.util.regex.Matcher valueMatcher = valuePattern.matcher(value);
                    if (nameMatcher.matches() && valueMatcher.matches()){
                        return nameMatcher.group(1).equals(valueMatcher.group(1));
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Does not conform to sample attributeMatcher");
            }
        };
    }

    @Test
    public void testCentos() throws Exception {

        File testFile = FileSystemTestUtils.getTestFile("org/jnode/fs/xfs/centos-xfs.img");
        try (FileDevice device = new FileDevice(testFile, "r")) {
            XfsFileSystemType type = new XfsFileSystemType();
            XfsFileSystem fs = type.create(device, true);
            XfsEntry entry = fs.getRootEntry();
            StringBuilder actual = new StringBuilder(1024);
            buildXfsDirStructure(entry, actual, "  ");
        } finally {
            testFile.delete();
        }
    }

    /**
     * Build the directory string.
     *
     * @param entry  the entry to process.
     * @param actual the string to append to.
     * @param indent the indent level.
     *
     * @throws IOException
     */
    private static void buildXfsDirStructure(XfsEntry entry,StringBuilder actual, String indent) throws IOException {

        actual.append(indent);
        actual.append(entry.getName() );
        actual.append("; \n");

        if (entry.isDirectory()) {
            FSDirectory directory = entry.getDirectory();

            Iterator<? extends FSEntry> iterator = directory.iterator();

            while (iterator.hasNext()) {
                FSEntry child = iterator.next();

                if ( ".".equals(child.getName()) || "..".equals(child.getName()) ) {
                    continue;
                }
                buildXfsDirStructure((XfsEntry)child, actual, indent + "  ");
            }
        }
    }
}
