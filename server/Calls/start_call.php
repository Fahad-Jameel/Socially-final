<?php
include '../connect.php'; // your DB connection here
include '../headers.php'; // your header file here

$caller_id = $_POST['caller_id'];
$receiver_id = $_POST['receiver_id'];

$sql = "INSERT INTO calls (caller_id, receiver_id, status) VALUES ('$caller_id', '$receiver_id', 'pending')";

if (mysqli_query($conn, $sql)) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "error" => mysqli_error($conn)]);
}
?>
