<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include "../connect.php";
$response = array();

if (isset($_POST['username'], $_POST['email'], $_POST['phoneno'], $_POST['password'])) {
    $username = $_POST['username'];
    $email    = $_POST['email'];
    $phoneno  = $_POST['phoneno'];
    $password = $_POST['password'];

    $sql = "INSERT INTO users (username, password, email, phoneno) VALUES ('$username', '$password', '$email', '$phoneno')";
    $result = mysqli_query($conn, $sql);

    if ($result) {
        $response['status'] = "success";
        $response['user_id'] = mysqli_insert_id($conn);
    } else {
        $response['status'] = "failure";
        $response['user_id'] = null;
    }
} else {
    $response['status'] = "failure";
    $response['user_id'] = null;
}

echo json_encode($response);
exit;
?>
