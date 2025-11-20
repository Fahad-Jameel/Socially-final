<?php
include "../connect.php";
include "../headers.php";

$response = array();

if (isset($_POST['email'], $_POST['password'])) {
    $email = $_POST['email'];
    $password = $_POST['password'];

    $sql = "SELECT id, password FROM users WHERE email = '$email'";
    $result = mysqli_query($conn, $sql);

    if ($result) {
        if (mysqli_num_rows($result) > 0) {
            $user = mysqli_fetch_assoc($result);

            if ($user['password'] === $password) {
                $response['success'] = true;
                $response['user_id'] = $user['id'];
            } else {
                $response['success'] = false;
                $response['message'] = "Invalid password";
            }
        } else {
            $response['success'] = false;
            $response['message'] = "User not found";
        }
    } else {
        $response['success'] = false;
        $response['message'] = "Database query failed";
    }
} else {
    $response['success'] = false;
    $response['message'] = "Email and password required";
}

echo json_encode($response);
?>
