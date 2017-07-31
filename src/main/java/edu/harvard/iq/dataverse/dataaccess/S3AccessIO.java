package edu.harvard.iq.dataverse.dataaccess;

import com.amazonaws.AmazonClientException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.partitions.PartitionsLoader;
import com.amazonaws.services.s3.model.GetObjectRequest;
import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DvObject;
import edu.harvard.iq.dataverse.datavariable.DataVariable;
import edu.harvard.iq.dataverse.util.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;


/**
 *
 * @author Matthew A Dunlap
 * @param <T> what it stores
 */
/* 
    Experimental Amazon AWS S3 driver
 */
public class S3AccessIO<T extends DvObject> extends StorageIO<T> {

    private static final Logger logger = Logger.getLogger("edu.harvard.iq.dataverse.dataaccess.S3AccessIO");

    public S3AccessIO() {
        this(null);
    }

    public S3AccessIO(T dvObject) {
        this(dvObject, null);
    }
    
    public S3AccessIO(T dvObject, DataAccessRequest req) {
        super(dvObject, req);
        this.setIsLocalFile(false);
    }

    //FIXME: Delete vars? 
    private boolean isReadAccess = false;
    private boolean isWriteAccess = false;
    private AWSCredentials awsCredentials = null;
    private AmazonS3 s3 = null;


    //FIXME: Copied, change?
    @Override
    public boolean canRead() {
        return isReadAccess;
    }

//FIXME: Copied, change?
    @Override
    public boolean canWrite() {
        return isWriteAccess;
    }

    
//    @Override
//    public DvObjectType getDvObjectType() {
//     
//    }
    
    //FIXME: Finish
    @Override
    public void open(DataAccessOption... options) throws IOException {
        DataAccessRequest req = this.getRequest();
        
        if (isWriteAccessRequested(options)) {
            isWriteAccess = true;
            isReadAccess = false;
        } else {
            isWriteAccess = false;
            isReadAccess = true;
        }
        
        //FIXME: Fill. Most of the content in here I (Matthew) don't really understand. copypaste
        if (dvObject instanceof DataFile) {
            DataFile dataFile = this.getDataFile();
            
            if (req != null && req.getParameter("noVarHeader") != null) {
                this.setNoVarHeader(true);
            }
            
            if (dataFile.getStorageIdentifier() == null || "".equals(dataFile.getStorageIdentifier())) {
                throw new IOException("Data Access: No local storage identifier defined for this datafile.");
            }
            if (isReadAccess) {
                
            } else if (isWriteAccess) {
                //create a real init function
                awsCredentials = new ProfileCredentialsProvider().getCredentials();
                s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(Regions.US_EAST_1).build();
                
                //saveInputStream();
                
                
                //I'm not sure what I actually need here. 
                //s3 does not use file objects like swift
                //maybe use putobjectrequest as an intermediate
                //...
                //maybe I just need an amazons3 object
            }

            this.setMimeType(dataFile.getContentType());

            try {
                this.setFileName(dataFile.getFileMetadata().getLabel());
            } catch (Exception ex) {
                this.setFileName("unknown");
            }
            
            
            
        } else if (dvObject instanceof Dataset) {
            
        } else if (dvObject instanceof Dataverse) {
            Dataverse dataverse = this.getDataverse();
        } else {
            throw new IOException("Data Access: Invalid DvObject type");
        }
        
    }

    // StorageIO method for copying a local Path (for ex., a temp file), into this DataAccess location:

    //FIXME: Incomplete
    @Override
    public void savePath(Path fileSystemPath) throws IOException {
        long newFileSize = -1;

        if (s3 == null || !this.canWrite()) {
            open(DataAccessOption.WRITE_ACCESS);
        }

        try {
            File inputFile = fileSystemPath.toFile();

            String bucketName = "testiqss-1239759fgsef34w4"; //name is global, no uppercase
           // s3.deleteBucket(bucketName);
            String key = "MyObjectKey12";
            
            if(!s3.doesBucketExist(bucketName))
            { 
                System.out.println("Rohit Bhattacharjee!!!! True");
                s3.createBucket(bucketName);
            }
            else
            { 
                System.out.println("Rohit Bhattacharjee!!!! False");
                
//                if(s3.doesObjectExist(bucketName, key)){
//                    System.out.println("Rohit Bhattacharjee File Exists!!");
//                }
//                else{
                    s3.putObject(new PutObjectRequest(bucketName, key, inputFile));
//                }
            
            newFileSize = inputFile.length();
            }

        } catch (SdkClientException ioex) {
            String failureMsg = ioex.getMessage();
            if (failureMsg == null) {
                failureMsg = "Swift AccessIO: Unknown exception occured while uploading a local file into a Swift StoredObject";
            }

            throw new IOException(failureMsg);
        }

        // if it has uploaded successfully, we can reset the size
        // of the object:
        
        //FIXME: do this?
        //setSize(newFileSize);
        
    }
    
    //FIXME: Empty
    @Override
    public void saveInputStream(InputStream inputStream) throws IOException {
        if (s3 == null || !this.canWrite()) {
            open(DataAccessOption.WRITE_ACCESS);
        }

    }
    
    //FIXME: Empty
    @Override
    public void delete() throws IOException {
    }

    //FIXME: Empty
    @Override
    public Channel openAuxChannel(String auxItemTag, DataAccessOption... options) throws IOException {
        return null;
    }

    //FIXME: Empty
    @Override
    public boolean isAuxObjectCached(String auxItemTag) throws IOException {
        return false;
    }
    
    //FIXME: Empty
    @Override
    public long getAuxObjectSize(String auxItemTag) throws IOException {
        return 0;
    }
    
    //FIXME: Empty
    @Override 
    public Path getAuxObjectAsPath(String auxItemTag) throws IOException {
        return null;
    }

    //FIXME: Empty
    @Override
    public void backupAsAux(String auxItemTag) throws IOException {
    }

    //FIXME: Empty
    @Override
    // this method copies a local filesystem Path into this DataAccess Auxiliary location:
    public void savePathAsAux(Path fileSystemPath, String auxItemTag) throws IOException {
    }
    
    //FIXME: Empty
    // this method copies a local InputStream into this DataAccess Auxiliary location:
    @Override
    public void saveInputStreamAsAux(InputStream inputStream, String auxItemTag) throws IOException {
    }
    
    //FIXME: Empty
    @Override
    public List<String>listAuxObjects() throws IOException {
        return null;
    }
    
    //FIXME: Empty
    @Override
    public void deleteAuxObject(String auxItemTag) throws IOException {
    }
    
    //FIXME: Empty
    @Override
    public void deleteAllAuxObjects() throws IOException {
    }
    
    //FIXME: Empty
    @Override
    public String getStorageLocation() {
        return null;
    }

    //FIXME: Empty
    @Override
    public Path getFileSystemPath() throws IOException {
        return null;
    }
    
    //FIXME: Empty
    @Override
    public boolean exists() throws IOException {
        return false;
    }

    //FIXME: Empty
    @Override
    public WritableByteChannel getWriteChannel() throws IOException {
        return null;
    }
    
    //FIXME: Empty
    @Override  
    public OutputStream getOutputStream() throws IOException {
        return null;
    }
    
    @Override
    public InputStream getAuxFileAsInputStream(String auxItemTag) throws IOException {
        return null;
    }

    @Override
    public String getSwiftContainerName() {
        return null;
    }
    
    // Auxilary helper methods, S3-specific:
    // // FIXME: Refer to swift implementation while implementing S3
    
    AmazonS3 authenticateWithS3(String swiftEndPoint) throws IOException {
        AWSCredentials credentials = getAWSCredentials();

        AmazonS3 s3 = null;

        try {
            s3 = AmazonS3ClientBuilder.standard().withCredentials(
                    new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_1).build();

        } catch (Exception ex) {
            throw new IOException("S3AccessIO: failed to authenticate S3" + ex.getMessage());
        }

        return s3;
    }
     
    //FIXME: It looks like the best way to do credentials is to change a jvm variable on launch
    //          and use the standard getCredentials. Test this.
    private AWSCredentials getAWSCredentials() {
        if(awsCredentials == null)
        {
            try {
                //We can leave this as is and set an env variable
                awsCredentials = new ProfileCredentialsProvider().getCredentials();
            } catch (Exception e) {
                throw new AmazonClientException(
                        "Cannot load the credentials from the credential profiles file. " +
                        "Please make sure that your credentials file is at the correct " +
                        "location (~/.aws/credentials), and is in valid format.",
                        e);
            }
        }
        
        return awsCredentials;
    }
    
    //FIXME: This method is used across the IOs, why repeat?
    private boolean isWriteAccessRequested (DataAccessOption... options) throws IOException {
        
        for (DataAccessOption option: options) {
            // In the future we may need to be able to open read-write 
            // Channels; no support, or use case for that as of now. 
            
            if (option == DataAccessOption.READ_ACCESS) {
                return false;
            }

            if (option == DataAccessOption.WRITE_ACCESS) {
                return true;
            }
        }
        
        // By default, we open the file in read mode:
        return false; 
    }
}
