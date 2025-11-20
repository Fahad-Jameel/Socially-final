<?php
include '../connect.php'; // your DB connection here
include '../headers.php'; // your header file here

$call_id = $_POST['call_id'];
$status = $_POST['status']; // accepted or rejected

$sql = "UPDATE calls SET status = '$status' WHERE call_id = '$call_id'";

if (mysqli_query($conn, $sql)) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "error" => mysqli_error($conn)]);
}
?>
