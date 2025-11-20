<?php
include "../connect.php";
include "../headers.php";
$response = array();

if(isset($_POST['id'], $_POST['bio'], $_POST['contact'], $_POST['username'], $_POST['picture'])) {
    
    $id       = (int)$_POST['id'];
    $bio      = $_POST['bio'];
    $contact  = $_POST['contact'];
    $username = $_POST['username'];
    $picture  = $_POST['picture'];

    $sql = "INSERT INTO profile (id, bio, contact, username, picture) 
            VALUES (?, ?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE 
                bio = VALUES(bio), 
                contact = VALUES(contact), 
                username = VALUES(username), 
                picture = VALUES(picture)";
    
    $stmt = $conn->prepare($sql);

    if($stmt) {
        $stmt->bind_param("issss", $id, $bio, $contact, $username, $picture);
        $result = $stmt->execute();

        if($result) {
            $response['status'] = "success";
        } else {
            $response['status']  = "failure";
            $response['message'] = "Insert or update failed";
        }

        $stmt->close();
    } else {
        $response['status']  = "failure";
        $response['message'] = "Statement preparation failed";
    }

    $conn->close();

} else {
    $response['status']  = "failure";
    $response['message'] = "Missing required fields";
}

echo json_encode($response);
?>
