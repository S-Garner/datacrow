import { useState } from "react";
import { Button } from "react-bootstrap";
import "./dropdown.css";


type DropdownProps = {
	title: string;
	options: number[];
	handleSelectOption: (option: string) => void;
};

export default function Dropdown({
	title,
	options,
	handleSelectOption,
}: DropdownProps) {
	const [isOpen, setIsOpen] = useState(false);

	const toggleDropdown = () => {
		setIsOpen(!isOpen);
	};

	return (
		<div className="relative" id="pageSelect">
		
			<Button onClick={toggleDropdown} key="pageDropDown">
				<span>{title}</span>
			</Button>

			{isOpen && (
				<div style={{ display: "flex", flexWrap: "wrap" }} id="pageSelectOverview">
					{options.map((option) => (
						<a
							className="pagination-button"
							key={option}
							onClick={() => {
								handleSelectOption("" + option);
								setIsOpen(false);
							}}
						>
							{option}
						</a>
					))}
				</div>
			)}
		</div>
	);
}
