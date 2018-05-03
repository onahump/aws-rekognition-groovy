import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.List
import java.io.IOException
import groovy.json.*
import java.nio.file.Paths

import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.model.S3Object
import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.util.IOUtils
import com.amazonaws.services.rekognition.model.BoundingBox
import com.amazonaws.services.rekognition.model.CompareFacesMatch
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.CompareFacesResult
import com.amazonaws.services.rekognition.model.ComparedFace


class CompareFacesExample {

   static void main(String[] args) throws Exception{

      String bucketName = "bucket-name"
      String file = "file-name"
      String file_path = "src/source/${file}.jpg"
      String key_name = Paths.get(file_path).getFileName().toString()
      String folderKey = "--name-directory-- + /"
      Float similarityThreshold = 70F
      AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient()

      //Uploading file
      s3.putObject(bucketName, key_name, new File(file_path))

      // All objects on Directory
      List<S3Objects> objectos = []
      List<String> keyNames = []

      for ( S3ObjectSummary summary : S3Objects.withPrefix(s3, bucketName, folderKey) ) {
        keyNames.add(summary.key)
        objectos.add(summary)
        //System.out.printf("Object with key '%s'n", summary.getKey());
      }
      keyNames.remove(folderKey)



      keyNames.each{name ->

        S3Object sourceImage = new S3Object().withBucket(bucketName).withName("${file}.jpg")
        S3Object targetImage = new S3Object().withBucket(bucketName).withName(name)

          AWSCredentials credentials
          try {
               credentials = new ProfileCredentialsProvider("adminuser").getCredentials()
          } catch (Exception e) {
              throw new AmazonClientException("""Cannot load the credentials from the credential profiles file.
              Please make sure that your credentials file is at the correct
              location (/Users/userid/.aws/credentials), and is in valid format.""", e)
          }

          AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                   .standard()
                   .withRegion(Regions.US_EAST_1)
                   .withCredentials(new AWSStaticCredentialsProvider(credentials))
                   .build()
           //Load source and target images and create input parameters


           Image source =new Image().withS3Object(sourceImage)


           Image target =new Image().withS3Object(targetImage)


           CompareFacesRequest request = new CompareFacesRequest()
                   .withSourceImage(source)
                   .withTargetImage(target)
                   .withSimilarityThreshold(similarityThreshold)

           // Call operation
           CompareFacesResult compareFacesResult=rekognitionClient.compareFaces(request)

          if(compareFacesResult.faceMatches.size() > 0){
            println "\nMatch with ${name} image!\n"
          }else{
            println "No se encontro parecido en la imagen ${name}"
          }
      }

           //println new JsonBuilder(compareFacesResult).toPrettyString()

   }
}

